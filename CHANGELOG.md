Change Log
=========
## Version 1.0.16
* Fixed bug x + width must be <= bitmap.width() (#40)

## Version 1.0.15
* Added code for preventing java.lang.IllegalArgumentException: bug x + width must be <= bitmap.width()(#40)

## Version 1.0.14
* remove "application android:label" from Manifest of library(#39)

## Version 1.0.13
* Added setMinFrameSizeInPx(#27,#28)

## Version 1.0.12
* Fixed bugs related to drawable.(#31)

## Version 1.0.11
* Fixed runtime exception when parcelling, added CREATOR(#17,#25)
* Fixed logic for setting image
* Added setImageURI(#19, #24)

## Version 1.0.10
* Fixed bug caused by float precision.(#20)

## Version 1.0.9

* Added getActualCropRect()(#14)
* Added setImageDrawable()(#12)
* Added getRectBitmap()(#16)

## Version 1.0.8

* Added rotateImage()(#2,#10)
* Added CropMode.Circle(#3,#9)
* Added setInitialFrameScale()(#4)
