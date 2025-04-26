# TestProject

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)
![R2DBC](https://img.shields.io/badge/R2DBC-1.0.0-blue)
![H2](https://img.shields.io/badge/H2-2.3.232-orange)

## 📖 소개

`TestProject`는 S3버켓에서 tif파일을 다운로드 받아 COG로 변환 후 목표 S3버켓에 저장합니다. GeoTIFF 메타데이터를 저장·조회하고, 로깅 테이블을 통해 업로드 기록을 관리하는 **Spring Boot Reactive** 애플리케이션입니다.  
- **Database**: H2 (in-memory/file) + R2DBC
- **Web**: Spring WebFlux  
- **Language**: Kotlin  

## ⚙️ 기술 스택

| 구성요소            | 버전          |
|-------------------|-------------|
| Spring Boot       | 3.4.4       |
| Spring Data R2DBC | 3.4.4       |
| R2DBC H2 Driver   | 1.0.0.RELEASE |
| Kotlin            | 1.9.25      |
| Java              | 23          |


## 🛠️ 설치 및 실행

1. 레포지토리 클론  
   ```bash
   git clone https://github.com/yourname/TestProject.git
   cd TestProject
