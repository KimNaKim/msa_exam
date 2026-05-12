# MSA 기반 주문 및 재고 관리 시스템

이 프로젝트는 Spring Boot와 Kafka를 활용하여 Saga 패턴(Choreography-based)을 구현한 마이크로서비스 아키텍처(MSA) 예제입니다.

## 🏗 아키텍처 개요

- **Order Service**: 주문 생성 및 상태 관리를 담당합니다.
- **Product Service**: 상품 관리 및 재고 차감을 담당합니다.
- **Kafka**: 서비스 간 비동기 이벤트 통신을 위한 메시지 브로커입니다.
- **MySQL**: 각 서비스의 데이터를 저장하는 데이터베이스입니다. (서비스별 독립 DB 사용)

## 🚀 구현된 주요 기능

### 1. 주문 프로세스 (Saga 패턴 - Choreography)
이 프로젝트는 분산 트랜잭션 환경에서 데이터 일관성을 유지하기 위해 **Choreography 기반 Saga 패턴**을 사용합니다. 각 서비스는 자신의 로컬 트랜잭션을 처리하고 이벤트를 발행하며, 다른 서비스의 이벤트를 구독하여 다음 단계를 처리합니다.

#### 정상 흐름 (Happy Path)
1. **주문 생성**: `Order Service`에서 주문을 `PENDING` 상태로 DB에 저장하고 `order-create` 이벤트를 발행합니다.
2. **재고 차감**: `Product Service`가 이벤트를 수신하여 DB의 재고를 차감하고 `product-stock-deducted` 이벤트를 발행합니다.
3. **주문 완료**: `Order Service`가 이벤트를 수신하여 주문 상태를 `COMPLETED`로 변경합니다.

#### 보상 트랜잭션 (Compensating Transaction)
분산 환경에서는 단일 데이터베이스의 ACID 트랜잭션을 사용할 수 없으므로, 하위 서비스에서 작업이 실패할 경우 이전 서비스들이 수행한 작업을 취소하거나 상태를 되돌리는 **보상 트랜잭션**이 필수적입니다.

1. **실패 발생**: `Product Service`에서 재고가 부족하거나 상품을 찾을 수 없는 경우, 재고 차감 없이 `product-stock-failed` 이벤트를 발행합니다.
2. **상태 롤백(보상)**: `Order Service`는 이 이벤트를 구독하고 있으며, 실패 이벤트를 수신하면 해당 주문의 상태를 `PENDING`에서 `CANCELLED`로 변경합니다.
    - 이는 이미 커밋된 `PENDING` 상태의 주문을 논리적으로 취소(Undo)하는 작업입니다.
3. **결과**: 최종적으로 주문 시스템과 재고 시스템 간의 데이터 일관성이 보장됩니다.

### 2. 서비스별 API

#### Order Service (`port 8082`)
- `GET /api/orders`: 전체 주문 목록 조회
- `POST /api/orders`: 새로운 주문 생성
    - Body: `{"productId": 1, "quantity": 2}`

#### Product Service (`port 8081`)
- `GET /api/products`: 전체 상품 목록 및 재고 조회
- `POST /api/products`: 상품 등록
- `POST /api/products/{id}/reduce-stock`: 수동 재고 차감 (테스트용)

## 🧪 테스트 시나리오

현재 구현된 로직을 바탕으로 다음과 같은 시나리오를 테스트할 수 있습니다.

### 시나리오 1: 주문 성공 (재고 충분)
1. `GET /api/products`로 상품 ID와 현재 재고를 확인합니다.
2. 재고보다 적은 수량으로 `POST /api/orders` 주문을 생성합니다.
3. `GET /api/orders`를 조회하여 상태가 `PENDING` -> `COMPLETED`로 변경되는지 확인합니다.
4. `GET /api/products`를 조회하여 재고가 주문 수량만큼 감소했는지 확인합니다.

### 시나리오 2: 주문 실패 (재고 부족)
1. 재고보다 많은 수량으로 `POST /api/orders` 주문을 생성합니다.
2. `GET /api/orders`를 조회하여 상태가 `PENDING` -> `CANCELLED`로 변경되는지 확인합니다.
3. `GET /api/products`를 조회하여 재고가 변하지 않았는지 확인합니다.

### 시나리오 3: 존재하지 않는 상품 주문
1. 존재하지 않는 `productId`로 주문을 생성합니다.
2. `Product Service`에서 예외가 발생하고, `Order Service`에서 해당 주문이 `CANCELLED`로 처리되는지 확인합니다.

## 🛠 실행 방법

1. **인프라 실행**: 프로젝트 루트에서 Docker Compose를 사용하여 MySQL과 Kafka를 실행합니다.
   ```bash
   docker-compose up -d
   ```
2. **서비스 실행**:
   - `order-service`와 `product-service`를 각각 실행합니다. (Gradle bootRun 등)
3. **데이터 초기화**: `infra/mysql/init/init.sql`이 컨테이너 실행 시 자동으로 로드되어 기본 상품 데이터가 생성됩니다.
