### 基于json_serializable的辅助工具

使用方式有两种:

    1.右键或快捷键进入Generate
    2.idea菜单栏Build->AutoAssistJson

JsonSerializable: 生成json_serializable需要的代码,然后终端运行flutter pub run build_runner build --delete-conflicting-outputs命令

JsonSerializable-Generate: 只生成json_serializable需要的代码

JsonSerializable-Build: 只在终端运行flutter pub run build_runner build --delete-conflicting-outputs命令