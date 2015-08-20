
![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/cover-art.png)

#SimpleCropView

The SimpleCropView is an image cropping library for Android.<br>
It simplify your code for cropping image and provides easily customizable UI.<br><br>
Supported on API Level 9 and above.


![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_basic_usage.gif)

##Table of Contents
* [Download](#download) 
* [Basic Usage](#basic-usage) 
* [Customization](#customization) 
  * [CropMode](#cropmode)
  * [MinimumFrameSize](#minimumframesize)
  * [Color](#color)
  * [Stroke Weight and Handle Size](#stroke-weight-and-handle-size)
  * [Handle Touch Padding](#handle-touch-padding)
  * [Handle and Guide ShowMode](#handle-and-guide-showmode)
* [XML Attributes](#xml-attributes) 
* [Developed By](#developed-by) 
* [License](#license) 

##Download
Include the following dependency in your `build.gradle` file.

```groovy
repositories {
    jcenter()
}
dependencies {
    compile 'com.isseiaoki:simplecropview:1.0.7'
}
```

##Basic Usage

Add the `com.isseiaoki.simplecropview.CropImageView` to your layout XML file.

>**NOTE:** The image is scaled to fit the size of the view by maintaining the aspect ratio. `WRAP_CONTENT` will be ignored.

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:orientation="vertical">

    <com.isseiaoki.simplecropview.CropImageView
        android:id="@+id/cropImageView"
        xmlns:custom="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="250dp"
        android:padding="16dp"
        custom:cropMode="ratio_1_1"
        />

    <Button
        android:id="@+id/crop_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_margin="16dp"
        android:text="CROP"
        />

    <ImageView
        android:id="@+id/croppedImageView"
        android:layout_margin="16dp"
        android:layout_gravity="center"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        />

</LinearLayout>
```


Set image, and get cropped image.


```java
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
        final ImageView croppedImageView = (ImageView)findViewById(R.id.croppedImageView);

        // Set image for cropping
        cropImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.sample5));
        
        Button cropButton = (Button)findViewById(R.id.crop_button);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get cropped image, and show result.
                croppedImageView.setImageBitmap(cropImageView.getCroppedBitmap());
            }
        });
    }

}
```

For a working implementation of this project see the `simplecropview-sample` folder.


##Customization

[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb1.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase1.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb3.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase3.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb4.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase4.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb5.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase5.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb6.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase6.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb7.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase7.jpg)

###CropMode

The option for the aspect ratio of the image cropping frame.

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9);
```

>**CropMode Values:**
>```
RATIO_4_3, RATIO_3_4, RATIO_1_1, RATIO_16_9, RATIO_9_16, RATIO_FIT_IMAGE, RATIO_FREE
```

>`RATIO_FREE`:  *non-Fixed aspect ratio mode*<br>
`RATIO_X_Y`:  *Fixed aspect ratio mode*<br>
`RATIO_FIT_IMAGE`:  *Fixed aspect ratio mode. The same aspect ratio as the original photo.*

>If you need other aspect ratio, use `setCustomRatio(int ratioX, int ratioY);`

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_crop_mode.gif)


###MinimumFrameSize
The minimum size of the image cropping frame in dp.

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setMinFrameSizeInDp(100);
```

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_minimum_frame_size.gif)

###Color

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setBackgroundColor(0xFFFFFFFB);
cropImageView.setOverlayColor(0xAA1C1C1C);
cropImageView.setFrameColor(getResources().getColor(R.color.frame));
cropImageView.setHandleColor(getResources().getColor(R.color.handle));
cropImageView.setGuideColor(getResources().getColor(R.color.guide));
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/color-attributes.png)

###Stroke Weight and Handle Size

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setFrameStrokeWeightInDp(1);
cropImageView.setGuideStrokeWeightInDp(1);
cropImageView.setHandleSizeInDp(getResources().getDimension(R.dimen.handle_size));
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/size-attributes.png)

###Handle Touch Padding

Additional touch area for the image cropping frame handle.

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setTouchPadding(16);
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/handle-touch-padding.png)

###Handle and Guide ShowMode

```java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setHandleShowMode(CropImageView.ShowMode.SHOW_ALWAYS);
cropImageView.setGuideShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
```

>**ShowMode Values:**
>```
SHOW_ALWAYS, NOT_SHOW, SHOW_ON_TOUCH
```

| Handle ShowMode | Guide ShowMode | Appearance |
|:-------------:|:-------------:|:-----:|
| SHOW_ALWAYS | SHOW_ALWAYS | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/show_handle_and_guide.jpg" width="100%"> |
| NOT_SHOW | NOT_SHOW | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/not_show_handle_and_guide.jpg" width="100%"> |
| SHOW_ALWAYS | NOT_SHOW | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/show_handle_only.jpg" width="100%"> |
| SHOW_ALWAYS | SHOW_ON_TOUCH | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_guide_show_on_touch.gif" width="100%"> |
| SHOW_ON_TOUCH | NOT_SHOW | <img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_handle_show_on_touch.gif" width="100%"> |


##XML Attributes
XML sample here.

```xml
    <com.isseiaoki.simplecropview.CropImageView
        android:id="@+id/cropImageView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="32dp"
        custom:imgSrc="@mipmap/ic_launcher"
        custom:cropMode="ratio_fit_image"
        custom:minFrameSize="50dp"
        custom:backgroundColor="@color/background_material_dark"
        custom:overlayColor="#66000000"
        custom:frameColor="@android:color/white"
        custom:handleColor="@android:color/white"
        custom:guideColor="#BBFFFFFF"
        custom:frameStrokeWeight="3dp"
        custom:guideStrokeWeight="1dp"
        custom:handleSize="32dp"
        custom:touchPadding="0dp"
        custom:guideShowMode="not_show"
        custom:handleShowMode="show_always"
        custom:cropEnabled="true"
        />
```

| XML Attribute<br>(custom:) | Related Method | Description |
|:---|:---|:---|
| imgSrc | setImageResource(int resId) | Set source image. |
| cropMode | setCropMode(CropImageView.CropMode mode) | Set crop mode. |
| minFrameSize | setMinFrameSizeInDp(int minDp) | Set the image cropping frame minimum size in density-independent pixels. |
| backgroundColor | setBackgroundColor(int bgColor) | Set view background color. |
| overlayColor | setOverlayColor(int overlayColor) | Set image overlay color. |
| frameColor | setFrameColor(int frameColor) | Set the image cropping frame color. |
| handleColor | setHandleColor(int frameColor) | Set the handle color. |
| guideColor | setGuideColor(int frameColor) | Set the guide color. |
| handleSize | setHandleSizeInDp(int handleDp) | Set handle radius in density-independent pixels. |
| touchPadding | setTouchPaddingInDp(int paddingDp) | Set the image cropping frame handle touch padding(touch area) in density-independent pixels. |
| frameStrokeWeight | setFrameStrokeWeightInDp(int weightDp) | Set frame stroke weight in density-independent pixels. |
| guideStrokeWeight | setGuideStrokeWeightInDp(int weightDp) | Set guideline stroke weight in density-independent pixels. |
| guideShowMode | setGuideShowMode(CropImageView.ShowMode mode) | Set guideline show mode. |
| handleShowMode | setHandleShowMode(CropImageView.ShowMode mode) | Set handle show mode. |
| cropEnabled | setCropEnabled(boolean enabled) | Set whether to show the image cropping frame. |

##Developed By
 * Issei Aoki - <i.greenwood.dev@gmail.com>

##License
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
