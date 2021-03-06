## 常见问题

   ### 1. 如何修改程序运行端口
   
    修改管理程序命令文件中 --server.port=2122
        
   ### 2. 如何修改程序日志路径
   
    修改管理程序命令文件中 --jpom.log=/jpom/log/
        
   ### 3. 如何修改回话超时时长
        
    在管理程序命令文件中 ARGS 变量添加 --tomcat.sessionTimeOut=1800
        
   ### 4. jpom 启动提示
   
   ![jpom](/doc/error/ff-unix.png)
    
   执行如下命令：(https://blog.csdn.net/perter_liao/article/details/76757605)
   
    1.编辑文件
      #vim filename（文件名）
      
    2.进入末行模式（按esc键）
    
    3.设置文件格式
     ：set fileformat=unix
     
    4.保存退出
     ：wq
     
    5.#sh filename
      OK!
      
   ### 5. 启动提示
   
   > jpom 数据目录权限不足...
   
   请检查当前用户是否拥有对应目录的读写权限
   
   ### 6. Jpom使用Nginx代理推荐配置
   

```
server {
    #charset koi8-r;
    access_log  /var/log/nginx/jpom.log main;
    listen       80;
    server_name  jpom.xxxxxx.com;
    
    location / {
        proxy_pass   http://127.0.0.1:2122/;
        proxy_set_header Host      $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        client_max_body_size  50000m;
        client_body_buffer_size 128k;
        #  websocket 配置
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

   ### 7. 启动提示数据目录权限不足
    
   [https://gitee.com/keepbx/Jpom/wikis/pages?sort_id=1395625&doc_id=264493](https://gitee.com/keepbx/Jpom/wikis/pages?sort_id=1395625&doc_id=264493)
   
   ### 8. 启动提示JDK没有找到tools.jar
   
   [https://gitee.com/keepbx/Jpom/wikis/pages?sort_id=1398788&doc_id=264493](https://gitee.com/keepbx/Jpom/wikis/pages?sort_id=1398788&doc_id=264493)
   
   