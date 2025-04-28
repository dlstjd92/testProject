# Tiff convert project

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)
![R2DBC](https://img.shields.io/badge/R2DBC-1.0.0-blue)
![H2](https://img.shields.io/badge/H2-2.3.232-orange)

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