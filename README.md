# http
封装Retrofit Rxjava.okhttp网络请求及文件下载。
包括get post down

1.http 使用


   HttpManager.getInstance().GET(url, String.class, new HttpCallback() {
   
            @Override
            public void onSuccess(Object body) {
                Log.d("MainActivity", "onSuccess: " + body.toString());
            }
            
            @Override
            public void onFail(int responseCode) {

            }
            @Override
            public void onError(Exception e) {
            }
        });
        
2.Retrofit使用
  初始化 最好在application进行：
  
  RetrofitManager.getInstance().init(Api.BASE_URl);
  
     RetrofitManager.getInstance().getServer(Api.class)
                .getBaidu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        Log.d("MainActivity", "accept: " + responseBody.string());
                    }
                });
                
    下载
    
    RetrofitManager.getInstance().getServer(Api.class)
                .downBaidu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DownConsumer(fileName, new HttpDownCallback() {
                    @Override
                    public void onProgress(int progress) {
                        
                    }

                    @Override
                    public void onSuccess(File file) {

                    }

                    @Override
                    public void onFailure(int responseCode) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                }));

