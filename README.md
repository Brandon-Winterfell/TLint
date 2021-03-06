# HuPu-TL
根据虎扑新版api开发，基于`Dagger2`+`RxJava`+`Retrofit`+`Material Design`开发，使用mvp模式开发~纯练手之作，目前基本功能已完成，欢迎jrs来star和fork，有什么问题也可以提issue


你也可以在应用市场下载应用体验

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=com.gzsll.hupu)

[豌豆荚](http://www.wandoujia.com/apps/com.gzsll.hupu)

[本地下载](http://www.pursll.com/TLint_1.6.apk)


## 应用截图
![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot1.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot2.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot3.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot4.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot5.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot6.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot7.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot8.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot9.png) 

![](https://github.com/gzsll/TLint/raw/master/resource/Screenshot10.png) 

## api接口文档(相关实现在com.gzsll.hupu.api底下，使用retrofit进行请求)


>论坛相关接口

http://bbs.mobileapi.hupu.com/1/7.0.7/

#### 基本参数 
  - `client`  设备唯一id
  - `night`     夜间模式
  - `token`   登陆后的token
  - `sign`   签名信息(获取方法如下)
  
#### sign获取方法
``` java
 public String getRequestSign(Map<String, String> map) {
        ArrayList<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) {
                return lhs.getKey().compareTo(rhs.getKey());
            }
        });
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i = i + 1) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            Map.Entry<String, String> map1 = list.get(i);
            builder.append(map1.getKey()).append("=").append(map1.getValue());
        }
        builder.append("HUPU_SALT_AKJfoiwer394Jeiow4u309");
        return mSecurityHelper.getMD5(builder.toString());
    }
```

- 将所有请求参数按key在字典中的顺序排列
- 按照排序顺序依次将key、value值拼接起来
- 最后拼接`HUPU_SALT_AKJfoiwer394Jeiow4u309`，取拼接的值的md5
- 例如`http://bbs.mobile.hupu.com/notice/getMessageReply?sign=3f165118871621f467c83ebfc33393f6&lastId=0&platform=android&v=7.0&uuid=866470026532711&version=1.1`这个请求的拼接串为`lastId0platformandroiduuid866470026532711v7.0version1.13542e676b4c80983f6131cdfe577ac9b`，sign的值就为`3f165118871621f467c83ebfc33393f6`


> 其他接口请自行查看代码


## License

Copyright (c) 2014 pursll

Licensed under the [Apache License, Version 3.0](http://opensource.org/licenses/GPL-3.0)


