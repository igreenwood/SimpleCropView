![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/cover-art.png)

# SimpleCropView
[![build status](https://travis-ci.org/IsseiAoki/SimpleCropView.svg?branch=master)](https://travis-ci.org/IsseiAoki/SimpleCropView.svg?branch=master)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SimpleCropView-green.svg?style=flat)](https://android-arsenal.com/details/1/2366)
[![Android Gems](http://www.android-gems.com/badge/IsseiAoki/SimpleCropView.svg?branch=master)](http://www.android-gems.com/lib/IsseiAoki/SimpleCropView)

The SimpleCropView is an image cropping library for Android.<br>
It simplifies your code for cropping image and provides an easily customizable UI.<br><br>
Supported on API Level 14 and above.


![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.1.0/demo_basic_usage.gif)


## Table of Contents
* [Download](#download) 
* [Example](#example)
  * [Image Cropping](#image-cropping) 
  * [Image Rotation](#image-rotation)
* [Load Image](#load-image)
  * [Apply Thumbnail](#apply-thumbnail) 
* [Crop and Save Image](#crop-and-save-image)
  * [Compress Format](#compress-format)
  * [Compress Quality](#compress-quality)
* [Customization](#customization) 
  * [Maximum Output Size](#maximum-output-size)
  * [Fixed Output Size](#fixed-output-size)
  * [CropMode](#cropmode)
  * [MinimumFrameSize](#minimumframesize)
  * [InitialFrameScale](#initialframescale)
  * [Save and Restore FrameRect](#save-and-restore-framerect)
  * [Color](#color)
  * [Stroke Weight and Handle Size](#stroke-weight-and-handle-size)
  * [Handle Touch Padding](#handle-touch-padding)
  * [Handle and Guide ShowMode](#handle-and-guide-showmode)
  * [Animation](#animation)
* [Picasso and Glide Compatibility](#picasso-and-glide-compatibility)
* [Debug](#debug)
* [XML Attributes](#xml-attributes) 
* [For Xamarin](#for-xamarin)
* [Developed By](#developed-by)
* [Users](#users) 
* [License](#license) 

## Download
Include the following dependency in your `build.gradle` file. **Please use the latest version available.**

```groovy
repositories {
    jcenter()
}
dependencies {
    compile 'com.isseiaoki:simplecropview:1.1.8'
}
```

## Example

### Image Cropping

Add permission in `AndroidManifest.xml` file.

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

Add the `com.isseiaoki.simplecropview.CropImageView` to your layout XML file.

>**NOTE:** The image is scaled to fit the size of the view by maintaining the aspect ratio. `WRAP_CONTENT` will be ignored.

```xml       

<com.isseiaoki.simplecropview.CropImageView
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cropImageView"
    android:layout_weight="1"
    android:paddingTop="16dp"
    android:paddingBottom="16dp"
    android:paddingLeft="16dp"
    android:paddingRight="16dp"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    custom:scv_crop_mode="fit_image"
    custom:scv_background_color="@color/windowBackground"
    custom:scv_handle_color="@color/colorAccent"
    custom:scv_guide_color="@color/colorAccent"
    custom:scv_overlay_color="@color/overlay"
    custom:scv_frame_color="@color/colorAccent"
    custom:scv_handle_size="14dp"
    custom:scv_touch_padding="8dp"
    custom:scv_handle_show_mode="show_always"
    custom:scv_guide_show_mode="show_always"
    custom:scv_min_frame_size="50dp"
    custom:scv_frame_stroke_weight="1dp"
    custom:scv_guide_stroke_weight="1dp"/>

```

Load image from sourceUri.

```java

mCropView = (CropImageView) findViewById(R.id.cropImageView);

mCropView.load(sourceUri).execute(mLoadCallback);
```

with RxJava,

```
mCropView.load(sourceUri).executeAsCompletable();
```

Crop image and save cropped bitmap in saveUri.

```java
mCropView.crop(sourceUri)
    .execute(new CropCallback() {
  @Override public void onSuccess(Bitmap cropped) {
    mCropView.save(cropped)
        .execute(saveUri, mSaveCallback);
  }

  @Override public void onError(Throwable e) {
  }
});
```

with RxJava,

```
mCropView.crop(sourceUri)
    .executeAsSingle()
    .flatMap(new Function<Bitmap, SingleSource<Uri>>() {
      @Override public SingleSource<Uri> apply(@io.reactivex.annotations.NonNull Bitmap bitmap)
              throws Exception {
        return mCropView.save(bitmap)
            .executeAsSingle(saveUri);
      }
    })
    .subscribeOn(Schedulers.newThread())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Consumer<Uri>() {
      @Override public void accept(@io.reactivex.annotations.NonNull Uri uri) throws Exception {
        // on success
      }
    }, new Consumer<Throwable>() {
      @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable)
              throws Exception {
        // on error
      }
    });
```

### Image Rotation

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.1.0/demo_rotation.gif)

SimpleCropView supports rotation by 90 degrees.

```java

cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D); // rotate clockwise by 90 degrees
cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D); // rotate counter-clockwise by 90 degrees

```

**For a working implementation of this project, see [sample project](https://github.com/IsseiAoki/SimpleCropView/tree/master/simplecropview-sample).**

## Load Image

* `load(sourceUri).execute(mLoadCallback);`

with RxJava,

* `load(sourceUri).executeAsCompletable();`

These method load Bitmap in efficient size from sourceUri. 
You don't have to care for filePath and image size.
You can also use `Picasso` or `Glide`.

### Apply Thumbnail
You can use blurred image for placeholder.

```
mCropView.load(result.getData())
         .useThumbnail(true)
         .execute(mLoadCallback);
```

## Crop and Save Image

```java
mCropView.crop(sourceUri)
    .execute(new CropCallback() {
  @Override public void onSuccess(Bitmap cropped) {
    mCropView.save(cropped)
        .execute(saveUri, mSaveCallback);
  }

  @Override public void onError(Throwable e) {
  }
});
```

with RxJava,

```
mCropView.crop(sourceUri)
    .executeAsSingle()
    .flatMap(new Function<Bitmap, SingleSource<Uri>>() {
      @Override public SingleSource<Uri> apply(@io.reactivex.annotations.NonNull Bitmap bitmap)
              throws Exception {
        return mCropView.save(bitmap)
                .executeAsSingle(saveUri);
      }
    })
    .subscribeOn(Schedulers.newThread())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Consumer<Uri>() {
      @Override public void accept(@io.reactivex.annotations.NonNull Uri uri) throws Exception {
        // on success
      }
    }, new Consumer<Throwable>() {
      @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable)
              throws Exception {
        // on error
      }
    });
```

These cropping method use full size bitmap taken from `sourceUri` for cropping. 
If `sourceUri` is null, the Uri set in load(Uri) is used.
After cropping, it saves cropped image in `saveUri`.

### Compress Format

You can use 3 compress format, `PNG`(default),`JPEG`, and `WEBP`.

```java
setCompressFormat(Bitmap.CompressFormat.JPEG);
```

### Compress Quality
You can also set compress quality. `0`~`100`(default)

```java
setCompressQuality(90);
```

## Customization

[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb1.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase1.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb3.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase3.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb4.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase4.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb5.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase5.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb6.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase6.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb7.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase7.jpg)

### Maximum Output Size
You can set max size for output image. The output image will be scaled within given rect.

```java
setOutputMaxSize(300, 300);
```

### Fixed Output Size
You can also set fixed output width/height. 

```java
setOutputWidth(100); // If cropped image size is 400x200, output size is 100x50
``` 

```java
setOutputHeight(100); // If cropped image size is 400x200, output size is 200x100
``` 

### CropMode

The option for the aspect ratio of the image cropping frame.

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9);
```

#### Values
```
FIT_IMAGE, RATIO_4_3, RATIO_3_4, SQUARE(default), RATIO_16_9, RATIO_9_16, FREE, CUSTOM, CIRCLE, CIRCLE_SQUARE
```
#### Rect Crop
`FREE`:  *Non-Fixed aspect ratio mode*
`RATIO_X_Y`, `SQUARE`:  *Fixed aspect ratio mode*
`FIT_IMAGE`:  *Fixed aspect ratio mode. The same aspect ratio as the original photo.*

If you need other aspect ratio, use `setCustomRatio(int ratioX, int ratioY);`

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.1.0/demo_crop_mode_rect.gif)


#### Circle Crop

`CIRCLE`: *Fixed aspect ratio mode. Crop image as circle.*
`CIRCLE_SQUARE`: *Fixed aspect ratio mode. Show guide circle, but save as square.(`getRectBitmap()` is removed.)*


![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.1.0/demo_crop_mode_circle.gif)


### MinimumFrameSize
The minimum size of the image cropping frame in dp.(default:50)

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setMinFrameSizeInDp(100);
```

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_minimum_frame_size.gif)

### InitialFrameScale
The initial frame size of the image cropping frame. `0.01`~`1.0`(default)

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setInitialFrameScale(1.0f);
```

| scale | Appearance |
|:-------------:|:-----:|
| 0.5 | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.0.8/initial_frame_scale_0.5.jpg" width="100%"> |
| 0.75| <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.0.8/initial_frame_scale_0.75.jpg" width="100%"> |
| 1.0 (default)| <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.0.8/initial_frame_scale_1.0.jpg" width="100%"> |

### Save and Restore FrameRect
You can save and restore frame rect as follows. See [sample project](https://github.com/IsseiAoki/SimpleCropView/tree/master/simplecropview-sample) for more details.

* Save FrameRect

```
mCropView.getActualCropRect()
```

* Restore FrameRect

```
mCropView.load(result.getData())
         .initialFrameRect(mFrameRect)
         .execute(mLoadCallback);
```

### Color

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setBackgroundColor(0xFFFFFFFB);
cropImageView.setOverlayColor(0xAA1C1C1C);
cropImageView.setFrameColor(getResources().getColor(R.color.frame));
cropImageView.setHandleColor(getResources().getColor(R.color.handle));
cropImageView.setGuideColor(getResources().getColor(R.color.guide));
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/color-attributes.png)

### Stroke Weight and Handle Size

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setFrameStrokeWeightInDp(1);
cropImageView.setGuideStrokeWeightInDp(1);
cropImageView.setHandleSizeInDp(getResources().getDimension(R.dimen.handle_size));
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/size-attributes.png)

### Handle Touch Padding

Additional touch area for the image cropping frame handle.

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setTouchPadding(16);
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/handle-touch-padding.png)

### Handle and Guide ShowMode

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setHandleShowMode(CropImageView.ShowMode.SHOW_ALWAYS);
cropImageView.setGuideShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
```

#### Values
```
SHOW_ALWAYS(default), NOT_SHOW, SHOW_ON_TOUCH
```

| Handle ShowMode | Guide ShowMode | Appearance |
|:-------------:|:-------------:|:-----:|
| SHOW_ALWAYS | SHOW_ALWAYS | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/show_handle_and_guide.jpg" width="100%"> |
| NOT_SHOW | NOT_SHOW | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/not_show_handle_and_guide.jpg" width="100%"> |
| SHOW_ALWAYS | NOT_SHOW | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/show_handle_only.jpg" width="100%"> |
| SHOW_ALWAYS | SHOW_ON_TOUCH | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_guide_show_on_touch.gif" width="100%"> |
| SHOW_ON_TOUCH | NOT_SHOW | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_handle_show_on_touch.gif" width="100%"> |

### Animation
SimpleCropView supports rotate animation and frame change animation.

#### Enabled
Toggle whether to animate. `true` is default.

```java
setAnimationEnabled(true);
```

#### Duration
Set animation duration in milliseconds. `100` is default.

```java
setAnimationDuration(200);
```

#### Interpolator
Set interpolator of animation. `DecelerateInterpolator` is default.
You can also use your custom interpolator.

```java
setInterpolator(new AccelerateDecelerateInterpolator());
```

## Picasso and Glide Compatibility
`com.isseiaoki.simplecropview.CropImageView` is a kind of `ImageView`.
You can use it with Picasso or Glide as follows:

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
Picasso.with(context).load(imageUrl).into(cropImageView);
```
or

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
Glide.with(context).load(imageUrl).into(cropImageView);
```

>Some option does not work correctly because CropImageView does not support ImageView.ScaleType.

## Debug
You can use debug display.

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/1.1.0/demo_debug.gif)

```java
setDebug(true);
```

## XML Attributes
XML sample here.

```xml
<com.isseiaoki.simplecropview.CropImageView
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cropImageView"
    android:layout_weight="1"
    android:paddingTop="16dp"
    android:paddingBottom="16dp"
    android:paddingLeft="16dp"
    android:paddingRight="16dp"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    custom:scv_img_src="@drawable/sample5"
    custom:scv_crop_mode="fit_image"
    custom:scv_background_color="@color/windowBackground"
    custom:scv_overlay_color="@color/overlay"
    custom:scv_frame_color="@color/colorAccent"
    custom:scv_handle_color="@color/colorAccent"
    custom:scv_guide_color="@color/colorAccent"
    custom:scv_guide_show_mode="show_always"
    custom:scv_handle_show_mode="show_always"
    custom:scv_handle_size="14dp"
    custom:scv_touch_padding="8dp"
    custom:scv_min_frame_size="50dp"
    custom:scv_frame_stroke_weight="1dp"
    custom:scv_guide_stroke_weight="1dp"
    custom:scv_crop_enabled="true"
    custom:scv_initial_frame_scale="1.0"
    custom:scv_animation_enabled="true"
    custom:scv_animation_duration="200"
    custom:scv_handle_shadow_enabled="true"/>
```

| XML Attribute<br>(custom:) | Related Method | Description |
|:---|:---|:---|
| scv_img_src | setImageResource(int resId) | Set source image. |
| scv_crop_mode | setCropMode(CropImageView.CropMode mode) | Set crop mode. |
| scv_background_color | setBackgroundColor(int bgColor) | Set view background color. |
| scv_overlay_color | setOverlayColor(int overlayColor) | Set image overlay color. |
| scv_frame_color | setFrameColor(int frameColor) | Set the image cropping frame color. |
| scv_handle_color | setHandleColor(int frameColor) | Set the handle color. |
| scv_guide_color | setGuideColor(int frameColor) | Set the guide color. |
| scv_guide_show_mode | setGuideShowMode(CropImageView.ShowMode mode) | Set guideline show mode. |
| scv_handle_show_mode | setHandleShowMode(CropImageView.ShowMode mode) | Set handle show mode. |
| scv_handle_size | setHandleSizeInDp(int handleDp) | Set handle radius in density-independent pixels. |
| scv_touch_padding | setTouchPaddingInDp(int paddingDp) | Set the image cropping frame handle touch padding(touch area) in density-independent pixels. |
| scv_min_frame_size | setMinFrameSizeInDp(int minDp) | Set the image cropping frame minimum size in density-independent pixels. |
| scv_frame_stroke_weight | setFrameStrokeWeightInDp(int weightDp) | Set frame stroke weight in density-independent pixels. |
| scv_guide_stroke_weight | setGuideStrokeWeightInDp(int weightDp) | Set guideline stroke weight in density-independent pixels. |
| scv_crop_enabled | setCropEnabled(boolean enabled) | Set whether to show the image cropping frame. |
| scv_initial_frame_scale | setInitialFrameScale(float initialScale) | Set Set initial scale of the frame.(0.01 ~ 1.0) |
| scv_animation_enabled | setAnimationEnabled(boolean enabled) | Set whether to animate. |
| scv_animation_duration | setAnimationDuration(int durationMillis) | Set animation duration. |
| scv_handle_shadow_enabled | setHandleShadowEnabled(boolean handleShadowEnabled) | Set whether to show handle shadows. |

## Developed By
Issei Aoki - <i.greenwood.dev@gmail.com>
 
## Users
* [Snipping Tool - Screen Capture](https://play.google.com/store/apps/details?id=com.anhlt.sniptool)

If you are using my library, please let me know your app name : )

## For Xamarin
[https://bitbucket.org/markjackmilian/xam.droid.simplecropview](https://bitbucket.org/markjackmilian/xam.droid.simplecropview)

Thanks a million to Marco!!!

## License
```
The MIT License (MIT)

Copyright (c) 2015 Issei Aoki

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
