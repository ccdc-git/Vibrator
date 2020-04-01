package fragments

open class CustomCallbackActions {
    open fun onLeftClicked(position: Int) {}
    open fun onRightClicked(position: Int) {}
}
open class OnCustomInput{
    open fun itemClicked(codeName : String){}
}