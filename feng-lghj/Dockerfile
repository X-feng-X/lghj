# 基础镜像
FROM openjdk:11.0-jre-buster
# 作者
MAINTAINER feng
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 拷贝jarbao
COPY lghj-server.jar /app.jar
# 入口
ENTRYPOINT ["java", "-jar", "/app.jar"]
