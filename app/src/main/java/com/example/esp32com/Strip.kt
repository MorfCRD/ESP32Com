package com.example.esp32com

class Strip {
        var scmd = "*"
        var brightness = 20
        var color = "000000"
        var mode = 0
        var speed = 1000
        var update_req = 1

        constructor(scmd:String, brightness: Int, color: String,
                 mode: Int, speed: Int, update_req: Int) {
                this.scmd = scmd
                this.brightness = brightness
                this.color = color
                this.mode = mode
                this.speed = speed
                this.update_req = update_req
        }
}
