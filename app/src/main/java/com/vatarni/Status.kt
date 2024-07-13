package com.vatarni


class Status (private  var status: String = null.toString(),
              private  var time: String = null.toString()
) {


    fun getStatus(): String {
        return status
    }

    fun getTime(): String {
        return time
    }

    fun setStatus(status: String) {
        this.status = status
    }

    fun setTime(time: String) {
        this.time = time
    }


}