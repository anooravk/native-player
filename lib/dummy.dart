import 'package:flutter/material.dart';

class DummyFile extends StatelessWidget {
  const DummyFile({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(""),),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Center(child: Text("Dummy Screen")),
        ],
      ),
    );
  }
}
