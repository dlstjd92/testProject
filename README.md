# Tiff convert project

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)
![R2DBC](https://img.shields.io/badge/R2DBC-1.0.0-blue)
![H2](https://img.shields.io/badge/H2-2.3.232-orange)

jar 다운로드 : https://drive.google.com/file/d/12LgPJcZOk6Q6KMyhKjJAX7CTZvPMg-lD/view?usp=drive_link

파일이름 : testProject-boot.jar

## 소개

S3 버킷에서 TIFF 파일을 다운로드하여 COG 포맷으로 변환한 뒤, 목표 S3 버킷에 업로드하는 **Spring Boot Reactive** 애플리케이션. 

GeoTIFF 메타데이터를 저장·조회하고, 업로드 기록을 로깅 테이블에 관리합니다.

- **Database**: H2 (in-memory) + R2DBC
- **Web**: Spring WebFlux  
- **Language**: Kotlin

## 기술 스택

| 구성요소            | 버전            |
|--------------------|-----------------|
| Spring Boot        | 3.4.4           |
| Spring WebFlux     | 3.4.4           |
| Spring Data R2DBC  | 3.4.4           |
| R2DBC H2 Driver    | 1.0.0.RELEASE   |
| AWS SDK (S3, CRT)  | 2.20.x / 0.29.x |
| Kotlin             | 1.9.25          |
| Java               | 21              |

## 아키텍처 및 흐름

1. **AWS 인증정보 입력** (웹 페이지를 통해 Access Key ID / Secret Access Key 등록)
2. **S3 목록 조회** (원본 버킷 내 파일 리스트 확인)
3. **파일 선택 및 변환 요청** (선택된 TIFF 파일을 COG로 변환)
4. **변환 완료 후 목표 버킷으로 업로드**
5. **변환 및 업로드 기록 DB 저장**

## 설치 및 실행

1. 레포지토리 클론  
```bash
git clone https://github.com/dlstjd92/TestProject.git
cd TestProject
```

2. Gradle 빌드 및 실행  
```bash
./gradlew clean build
./gradlew bootRun
혹은 jar 파일 다운로드 후
java -jar testProject-boot.jar
```

3. 브라우저 접속  
```
http://localhost:8080
```

4. 웹페이지 상단에서 AWS Access Key ID / Secret Access Key 입력 후 사용
   - 조회할 버킷 입력 후 불러오기 클릭
   - 조회할 파일들 중 변환할 파일 체크
   - 업로드할 버킷명과 디렉토리명 입력
   - 변환 실행
   - 메타데이터 조회버튼으로 인메모리 DB에 저장된 파일 메타데이터 확인
   - 셀렉트 박스로 필터 검색 가능

![img_1.png](img_1.png)
## 주요 API 명세

| API Endpoint | 메소드 | 요청 데이터 | 설명                             |
|:-------------|:------|:------------|:-------------------------------|
| `/aws/credentials` | POST | `{ accessKeyId, secretAccessKey }` | AWS 인증 정보 등록 및 S3 클라이언트 초기화    |
| `/S3/getList` | POST | `{ bucketName }` | S3 버킷 내 파일 목록 조회               |
| `/CogConverter/convert` | POST | `{ bucketIn, key, bucketOut, targetKey }` | 단일 TIFF 파일을 변환 및 업로드 (웹 사용 안함) |
| `/CogConverter/batch-convert` | POST | `{ bucketIn, keys, bucketOut, targetKey }` | 여러 TIFF 파일을 일괄 변환 및 업로드        |
| `/logs` | GET | 없음 | 변환 및 업로드 기록 전체 조회 (웹 사용 안함)    |
| `/metadata` | GET | 없음 | 전체 GeoTIFF 메타데이터 조회            |
| `/metadata/filter` | GET | 쿼리 파라미터 (`filename`, `bucketName` 등) | 조건 기반 메타데이터 조회                 |

- **주의사항**: S3 작업 전에 반드시 AWS 인증정보를 입력해야 합니다.
- 입력된 인증정보는 메모리 상에만 저장되며 서버에 따로 저장되지 않습니다.

## 사용한 외부 라이브러리 및 목적

| 라이브러리 | 목적 |
|:---|:---|
| Spring Boot | 애플리케이션 구동 및 설정 |
| Spring WebFlux | 비동기 논블로킹 웹 서버 구축 |
| Spring Data R2DBC | R2DBC를 통한 비동기 데이터베이스 접근 |
| R2DBC H2 Driver | 인메모리 H2 데이터베이스 연결 |
| AWS SDK for S3 | S3 버킷 파일 다운로드 및 업로드 API 호출 |
| AWS S3 Transfer Manager | 대용량 S3 파일 전송 최적화 |
| AWS CRT (Common Runtime) | 네이티브 전송 가속화 라이브러리 |

---

## EC2 인스턴스 이용한 각 스팩별 테스트 결과

| 인스턴스 타입 | vCPU | 메모리 | 네트워크 대역폭      | 스토리지 | 동시 처리 (concurrency) | 멀티파트 크기 | 총 소요 시간 (초) |
|:--------------|:----:|:------:|:---------------------|:---------|:-----------------------:|:-------------:|:-----------------:|
| t2.medium     | 2    | 4 GiB  | Low to Moderate       | EBS 전용 | 3                       | 64 MB         | 실패 (메모리 부족) |
| t3.medium     | 2    | 4 GiB  | 최대 5 Gbps           | EBS 전용 | 3                       | 64 MB         | 실패 (메모리 부족) |
| t3.large      | 2    | 8 GiB  | 최대 5 Gbps           | EBS 전용 | 3                       | 64 MB         | 894.54초          |
| t3.xlarge     | 4    | 16 GiB | 최대 5 Gbps           | EBS 전용 | 3                       | 64 MB         | 626.14초          |
| m6i.large     | 2    | 8 GiB  | 최대 12.5 Gbps        | EBS 전용 | 3                       | 64 MB         | 839.49초          |

---

### 추가 설명
- 최대 17.9GB의 고용량 파일 병렬작업에는 메모리 4GB는 부적합한것으로 판명. 필요시 병렬성을 줄여 구동할 수 있으나 속도가 현저히 느릴것으로 예상.
- t3.large(거의 15분)을 기준으로 t3.xlarge(컴퓨팅 파워 업그레이드), m6i.large(네트워크 대역폭 업그레이드)를 해본 결과
  처음 가정인 네트워크 속도가 해당 작업에 절대적일것이란 예상을 깨고 코어와 메모리를 업그레이드 하는것이 훨씬 유리한것으로 판명.
  추가로 메모리가 클수록 동시 작업 스레드수를 늘릴 수 있기 때문에 실험 결과보다 더 빠르게 작업하는것도 가능할것으로 예상됨.

---
### 인프라 구성

<img width="518" alt="image" src="https://github.com/user-attachments/assets/6a1ba7bf-e468-4ee0-bc95-b904d0af4d26" />
S3에 업로드 시 자동으로 작업이 진행되는 ECS - Fargate 서버리스 아키텍쳐 구성.

1. S3에 파일을 업로드 시 이벤트가 자동으로 트리거 되어 SQS Queue에 전달 됨
   - SQS Queue 선택이유 : 예상치 못한 트래픽이 몰리는 경우에도 작업이 누락되지 않고 Dead letter Queue를 사용해 폴링에 실패한 작업 재시도 가능.
2. SQS Queue에서 Lambda를 이용하여 Fargate 클러스터에 이벤트 전달
   - Eventbridge가 아닌 SQS + Lambda를 선택한 이유 : SQS는 Fargate에 이벤트 전달을 못함. 그래서 Eventbridge를 사용하거나 Lambda를 사용해야 하는 상황. 트래픽이 몰릴 경우 작업이 누락되지 않는것이 더 중요하다고 생각되어 해당 아키텍쳐 사용.
3. Fargate 클러스터는 별도 서버 관리 없이(서버리스) 필요에 따라 자동으로 작업을 실행하고, 트래픽에 맞게 스케일을 조정한다.
   - 로드벨런서(ALB) 및 오토스케일링정책 사용하여 트래픽에 맞게 유동적으로 스케일인/아웃을 하도록 설정. 원하는 어플리케이션과 실행 환경을 하나의 Docker 이미지로 패키징하여 배포하고 실행한다.
4. 작업을 수행하며 메타데이터는 Aurora Surverless 를 사용하여 추가적인 관리없이 자동으로 스케일인/아웃 되도록 설정
   - 작업이 과하지 않을 경우 단순 Aurora 사용해도 무방
6. 어플리케이션레벨에서 작업 완료 시 타겟 S3버켓으로 업로드
   - 선택사항으로 CloudWatch log 사용하여 상태 모니터링, 에러 탐지, 로그확인 등 할 수 있음.
