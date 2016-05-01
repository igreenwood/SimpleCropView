Change Log
=========
## Version 1.1.4
* Fix Overlay drawing is lacking when CropMode.CIRCLE

## Version 1.1.3
* Fix bug can't parcel a recycled bitmap
* Fix Overlay drawing is lacking when selecting certain photos

## Version 1.1.2
* Fix bug image ratio collapse with FREE as cropMode
* Fix bug can't parcel a recycled bitmap

## Version 1.1.1
* Fix bug EXIF orientation not applied

## Version 1.1.0
* Support large size image(load/crop)
* Improve rotation algorithm
* Drop support for SDK level 9
* Add CropMode 'CIRCLE_SQUARE'
* Remove getRectBitmap() (Use 'CIRCLE_SQUARE' mode instead)
* Shorten CropMode name(ex. RATIO_FIT_IMAGE -> FIT_IMAGE)
* Add prefix to attrs(ex. cropMode -> scv_crop_mode)
* Add animation
* Support maximum output size
* Support fixed output size(width/height)
* Add debug display

## Version 1.0.16
* Fix bug x + width must be <= bitmap.width() (#40)

## Version 1.0.15
* Add code for preventing java.lang.IllegalArgumentException: bug x + width must be <= bitmap.width()(#40)

## Version 1.0.14
* remove "application android:label" from Manifest of library(#39)

## Version 1.0.13
* Add setMinFrameSizeInPx(#27,#28)

## Version 1.0.12
* Fix bugs related to drawable.(#31)

## Version 1.0.11
* Fix runtime exception when parcelling and add CREATOR(#17,#25)
* Fix logic for setting image
* Add setImageURI(#19, #24)

## Version 1.0.10
* Fix bug caused by float precision.(#20)

## Version 1.0.9

* Add getActualCropRect()(#14)
* Add setImageDrawable()(#12)
* Add getRectBitmap()(#16)

## Version 1.0.8

* Add rotateImage()(#2,#10)
* Add CropMode.Circle(#3,#9)
* Add setInitialFrameScale()(#4)
