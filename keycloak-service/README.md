في هذه الحالة، لا يزال بإمكانك التحكم في كل شيء، ولكن بدلاً من استخدام "المسارات النسبية" (Relative Paths)، سنستخدم تقنية **"الشبكة الخارجية المشتركة" (External Shared Network)**.

هذه الطريقة تسمح للحاويات (Containers) بالتواصل مع بعضها البعض حتى لو كانت موجودة في مجلدات مختلفة تماماً، أو حتى لو تم تشغيل كل واحدة منها بملف `docker-compose.yml` منفصل.

إليك الخطوات لتنفيذ ذلك:

### 1. إنشاء شبكة موحدة في Docker
أول شيء تفعله (مرة واحدة فقط) هو إنشاء شبكة افتراضية يدوياً في نظام Docker لكي تنضم إليها جميع الخدمات لاحقاً:
```bash
docker network create my-micro-network
```

---

### 2. إعداد كل مشروع في مجلده الخاص
في كل مجلد مشروع (في أي مسار كان على جهازك)، ستنشئ ملف `docker-compose.yml` خاص به.

#### مثال لخدمة الموظفين (في مسارها الخاص):
```yaml
services:
  employee-service:
    build: .
    container_name: employee-service
    ports:
      - "8081:8081"
    networks:
      - my-micro-network # انضمام للشبكة الموحدة

networks:
  my-micro-network:
    external: true # نخبر Docker أن الشبكة موجودة مسبقاً ولا ينشئ واحدة جديدة
```

#### مثال للـ API Gateway (في مساره الخاص):
```yaml
services:
  api-gateway:
    build: .
    container_name: api-gateway
    ports:
      - "9090:9090"
    networks:
      - my-micro-network

networks:
  my-micro-network:
    external: true
```

---

### 3. التواصل بين الخدمات (DNS الداخلي)
بما أن جميع الخدمات أصبحت الآن في شبكة واحدة اسمها `my-micro-network` وتستخدم `external: true` في إعداداتها، يمكنها "رؤية" بعضها البعض باستخدام **أسماء الحاويات** (`container_name`).

**تعديل هام في إعدادات الـ Gateway (أينما كان مكانه):**
في ملف `application.yml` الخاص بالـ Gateway، ستكتب العناوين هكذا:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: employee-service
          uri: http://employee-service:8081 # نستخدم اسم الحاوية
        - id: department-service
          uri: http://department-service:8082
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/springboot # اسم حاوية كيكلوك
```

---

### 4. كيف تقوم بتشغيلهم جميعاً بضغطة واحدة؟ (The Master Script)
بدلاً من الدخول لكل مجلد يدوياً، يمكنك إنشاء "سكربت" بسيط (Bash Script) في أي مكان على جهازك يقوم بهذه المهمة:

أحي مجلداً جديداً وسمه `run-all.sh` وضع فيه:
```bash
#!/bin/bash

# تشغيل كيكلوك أولاً
cd /home/mustapha/projects/keycloak-folder
docker compose up -d

# تشغيل الموظفين
cd /path/to/another/folder/employee-service
docker compose up -d --build

# تشغيل البوابة
cd /home/mustapha/projects/idea/api-gateway
docker compose up -d --build

echo "All services are starting..."
```
*(لا تنسَ إعطاء صلاحية التشغيل للسكربت: `chmod +x run-all.sh`)*.

---

### الخلاصة:
١. **Docker Network:** هي "السلك" الذي يربط المشاريع ببعضها مهما كانت أماكنها على القرص الصلب.
٢. **External Configuration:** تخبر كل مشروع أن يستخدم الشبكة الموحدة.
٣. **Container Name:** هو "رقم الهاتف" الذي تستخدمه الخدمات للاتصال ببعضها (بدلاً من IP أو localhost).

**بهذه الطريقة، يمكنك تنظيم ملفاتك كما تشاء، وسيعمل النظام كميكروسيرفس واحدة متصلة.**