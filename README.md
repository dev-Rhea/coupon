# coupon payment system

📋 프로젝트 개요

쿠폰 결제 시스템은 대용량 트래픽과 높은 동시성을 처리할 수 있는 엔터프라이즈급 결제 플랫폼

🛠️ 기술 스택
#### Backend Framework

Spring Boot 3.2: 메인 애플리케이션 프레임워크
Spring Security: 인증/인가 처리
Spring Data JPA: ORM 및 데이터 접근 계층
Spring Batch: 대용량 배치 처리

#### Business Process Management

Camunda BPM 7.20: 워크플로우 엔진 및 프로세스 오케스트레이션
BPMN 2.0: 비즈니스 프로세스 모델링

#### Database & Cache

MySQL 8.0: 메인 데이터베이스 (트랜잭션 데이터)
Redis 7.0: 캐시 및 세션 스토어
Redisson: Redis 기반 분산 락 구현

#### Monitoring & Logging

SLF4J + Logback: 구조화된 로깅
Micrometer: 메트릭 수집 및 모니터링
Spring Actuator: 헬스체크 및 운영 정보

🏗️ 시스템 아키텍처

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Load Balance  │
│   (Mobile/Web)  │───▶│   (Spring MVC)  │───▶│   (Nginx)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
│
┌────────────────────────────────┼────────────────────────────────┐
│                                │                                │
┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
│ Coupon Service  │              │ Payment Service │              │Settlement Service│
│   (Core BL)     │◀────────────▶│  (Camunda BPM)  │◀────────────▶│  (Batch Job)    │
└─────────────────┘              └─────────────────┘              └─────────────────┘
│                                │                                │
┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
│ Redis Cluster   │              │   MySQL 8.0     │              │  Spring Batch   │
│ (Cache/Session) │              │  (Transaction)  │              │  (Scheduling)   │
└─────────────────┘              └─────────────────┘              └─────────────────┘


📦 모듈 구성

핵심 기능

🎫 coupon-core - 쿠폰 관리 핵심 모듈

✅ 쿠폰 검증 로직: 유효기간, 잔액, 가맹점 제한 등 종합 검증
✅ 실시간 잔액 관리: Redis 기반 고성능 잔액 캐싱 및 동시성 제어
✅ 분산 락 메커니즘: Redisson을 활용한 안전한 쿠폰 상태 관리
✅ 예약 시스템: 결제 진행 중 잔액 임시 예약으로 이중 사용 방지


💳 coupon-payment - 결제 처리 모듈

✅ Camunda BPM 워크플로우: 결제 전체 생명주기 관리
✅ PG사 연동: Mock PG 서비스를 통한 실제적인 결제 시뮬레이션
✅ 보상 트랜잭션: 각 단계 실패 시 자동 롤백 처리
✅ 비동기 처리: 알림, 로깅 등 부가 작업의 논블로킹 처리

[쿠폰 검증] → [PG 결제] → [쿠폰 확정] → [완료 처리]
↓            ↓           ↓            ↓
[잔액 복원]   [결제 취소]  [PG 취소]   [실패 처리]


💰 coupon-settlement - 정산 관리 모듈

✅ 자동 정산 처리: Spring Batch 기반 일간/월간 정산 배치
✅ 실시간 조정: 결제 취소/환불 시 즉시 정산 조정
✅ 정합성 검증: 거래 데이터와 정산 데이터 일치성 검증
✅ 리포트 생성: 가맹점별 상세 정산 리포트 제공




