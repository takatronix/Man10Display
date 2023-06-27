package red.man10.display
data class ConfigData (
    var broadcast:Boolean = false,
    var serverName:String = "",
    var switchTime: Int = 30,                 // タスク切替タイミング

    var mapMode:Int = 2,
    var mapSize: Int = 0,

    var mapWidth: Int = 0,
    var vcWidth: Int = 0,
    var vcHeight: Int = 0,
    var streamPort:Int = 1337
)