package io.edgedev.appextractor

/**
 * Created by OPEYEMI OLORUNLEKE on 1/15/2018.
 */
interface ClickedApp{
    fun onClickDownload(position: Int)
    fun onClickedOpen(position: Int)
    fun onClickedPlaystore(position: Int)
    fun onClickInfo(position: Int)
}
