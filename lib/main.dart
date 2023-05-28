
import 'package:flios/dummy.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io' show Platform;


void main() => runApp(MyApp());

// TODO testurl
// https://pubads.g.doubleclick.net/gampad/ads?iu=/6870/one_app/one_android_preroll&description_url=https%3A%2F%2Fwww.one.co.il%2F&tfcd=0&npa=0&sz=640x480&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=
class MyApp extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter IMA SDK Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}
var currentUrl="http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4";
var currentIndex=0;
class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> with WidgetsBindingObserver {

  var viewPlayerController;
  MethodChannel _channel;
  bool isNormalScreen = true;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _channel = MethodChannel('bms_video_player');
    _channel.setMethodCallHandler(_handleMethod);
  }

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case 'fullScreen':
        isNormalScreen = false;
        SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
        SystemChrome.setPreferredOrientations([
          DeviceOrientation.landscapeLeft,
          DeviceOrientation.landscapeRight,
        ]);
        setState(() {});
        break;
      case 'normalScreen':
        isNormalScreen = true;
        SystemChrome.setEnabledSystemUIMode(SystemUiMode.manual,
            overlays: [SystemUiOverlay.bottom, SystemUiOverlay.top]);
        SystemChrome.setPreferredOrientations([
          DeviceOrientation.portraitUp,
          DeviceOrientation.portraitDown,
        ]);
        setState(() {});
        break;
    }
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
    WidgetsBinding.instance.removeObserver(this);
    if (Platform.isIOS) {
      _channel.invokeMethod('pauseVideo', 'pauseVideo');
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        this.viewPlayerController.resumeVideo();
        break;
      case AppLifecycleState.paused:
        this.viewPlayerController.pauseVideo();
        break;
      default:
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    var x = 0.0;
    var y = 0.0;
    var width = 400.0;
    var height = isNormalScreen ? 400.0 : MediaQuery.of(context).size.height;

    BmsVideoPlayer videoPlayer = new BmsVideoPlayer(
        onCreated: onViewPlayerCreated,
        x: x,
        y: y,
        width: width,
        height: height,
        url: currentUrl,
    );
    int _selectedIndex = 0;
    List<String> contentUrls=[
      "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
    ];

    void _onItemTapped(int index) {
      this.viewPlayerController.pauseVideo();
      setState(() {
        _selectedIndex = index;
      });
      if(_selectedIndex!=0){
        Navigator.push(context, MaterialPageRoute(builder: (context)=>DummyFile()));
      }
    }

   var adUrl="https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=&pmad=4&video_duration=9000&vpos=preroll%2Cmidroll%2Cpostroll&preroll=1&postroll=1&pod=enabled&mridx=enabled";
    return Scaffold(
      appBar: isNormalScreen
          ? AppBar(
              title: Text("Video plugin"),
            )
          : null,
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex, //New
        onTap: _onItemTapped,
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.menu),
            label: 'Menu',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.camera_alt),
            label: 'Camera',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.chat),
            label: 'Chats',
          ),
        ],
      ),
      body: ListView.builder(
        itemCount: 1,
        itemBuilder: (BuildContext context, int index) {
          return Column(
            children: [
              Container(
                child: videoPlayer,
                width: width,
                height: height,
                color: Colors.black,
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  ElevatedButton(onPressed: (){
                    if(Platform.isAndroid) {
                          setState(() {
                            currentIndex--;
                            currentUrl =
                                contentUrls[currentIndex % contentUrls.length];
                            this
                                .viewPlayerController
                                .loadpre(currentUrl, adUrl);
                          });
                          print(currentUrl);
                        }else{
                      setState(() {
                        currentIndex--;
                        currentUrl =
                        contentUrls[currentIndex % contentUrls.length];
                        this
                            .viewPlayerController
                            .prev(currentUrl.toString(),adUrl.toString());
                      });
                      print(currentUrl);
                    }
                      }, child: Text("Previous")),
                  ElevatedButton(onPressed: (){
                        if(Platform.isAndroid) {
                          setState(() {
                            currentIndex++;
                            currentUrl =
                                contentUrls[currentIndex % contentUrls.length];
                            this
                                .viewPlayerController
                                .loadnew(currentUrl, adUrl);
                          });
                          print(currentUrl);
                        }else{
                          setState(() {
                            currentIndex++;
                            currentUrl =
                            contentUrls[currentIndex % contentUrls.length];
                            this
                                .viewPlayerController
                                .next(currentUrl.toString(),adUrl.toString());
                          });
                          print(currentUrl);
                        }
                      }, child: Text("Next")),
                ],
              )
            ],
          );
        },
      ),
    );
  }

  void onViewPlayerCreated(viewPlayerController) {
    this.viewPlayerController = viewPlayerController;
  }
}

class _VideoPlayerState extends State<BmsVideoPlayer> {
  String viewType = 'NativeUI';
  var viewPlayerController;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      child: nativeView()
    );
  }


  nativeView() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return
          AndroidView(
        viewType: viewType,
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String, dynamic>{
          "x": widget.x,
          "y": widget.y,
          "width": widget.width,
          "height": widget.height,
          "videoURL": currentUrl,
              // "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
          "adURL":
              "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=&pmad=4&video_duration=9000&vpos=preroll%2Cmidroll%2Cpostroll&preroll=1&postroll=1&pod=enabled&mridx=enabled"
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: viewType,
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String, dynamic>{
          "x": widget.x,
          "y": widget.y,
          "width": widget.width,
          "height": widget.height,
          "videoURL":currentUrl,
              // "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
          "adURL":
              "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpodbumper&cmsid=496&vid=short_onecue&correlator="
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    }

  }

  Future<void> onPlatformViewCreated(id) async {
    if (widget.onCreated == null) {
      return;
    }

    widget.onCreated(new BmsVideoPlayerController.init(id));
  }
}

typedef void BmsVideoPlayerCreatedCallback(BmsVideoPlayerController controller);

class BmsVideoPlayerController {
  MethodChannel _channel;

  BmsVideoPlayerController.init(int id) {
    _channel = new MethodChannel('bms_video_player');
  }

  Future<void> loadUrl(String url) async {
    assert(url != null);
    return _channel.invokeMethod('loadUrl', url);
  }


  Future<void> loadnew(String url,String adurl) async {
    assert(url != null);
    return _channel.invokeMethod('loadnew', url+"@_@"+adurl);
  }

  Future<void> loadpre(String url,String adurl) async {
    assert(url != null);
    return _channel.invokeMethod('loadpre', url+"@_@"+adurl);
  }

  Future<void> pauseVideo() async {
    return _channel.invokeMethod('pauseVideo', 'pauseVideo');
  }

  Future<void> resumeVideo() async {
    return _channel.invokeMethod('resumeVideo', 'resumeVideo');
  }

  Future<void> next(String url,String adurl) async {
    return _channel.invokeMethod('next', {"videoURL":url,"adURL":adurl});
  }

  Future<void> prev(String url,String adurl) async {
    return _channel.invokeMethod('next', {"videoURL":url,"adURL":adurl});
  }
}

class BmsVideoPlayer extends StatefulWidget {
  final BmsVideoPlayerCreatedCallback onCreated;
  final x;
  final y;
  final width;
  final height;
  final url;

  BmsVideoPlayer({
    Key key,
    @required this.onCreated,
    @required this.x,
    @required this.y,
    @required this.width,
    @required this.height,
    @required this.url,
  });

  @override
  State<StatefulWidget> createState() => _VideoPlayerState();
}



