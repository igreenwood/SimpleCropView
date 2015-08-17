#SimpleCropView
The SimpleCropView is an image cropping library for Android. <br>It simplify your code for cropping image and provides easily customizable UI.

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/cover-art.png)

###Simple Implementation
Implementation is very simple, it ends in three steps.

1. Set the image (both via XML and programmatically).
1. Adjust image cropping frame.
1. Call `getCroppedBitmap();`

>**Note:** <br>The image is scaled to fit the size of the view by maintaining the aspect ratio. Any remaining area of the view's bounds is transparent. **It is not possible to match the size of the view on the size of the image.<br> WRAP_CONTENT parameter will be ignored.**

###Easily Customizable UI
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb1.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase1.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb3.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase3.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb4.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase4.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb5.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase5.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb6.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase6.jpg)
[![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/thumbnails/thumb7.jpg)](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/device-art/showcase7.jpg)

Whether appearance of the View can fit your application, it is a very important issue.

The SimpleCropView provides following attributes for customizing view appearance.

* frame color
* handle color
* guide color
* background color
* frame overlay color
* frame stroke weight(dp)
* frame guideline stroke weight(dp)
* frame handle size(dp)
* frame handle touch padding(dp)
* guidle line show mode ( *show_always / not_show / show_on_touch* )
* handle show mode ( *show_always / not_show / show_on_touch* )


## Download
**build.gradle**

```
repositories {
    jcenter()
}
dependencies {
    compile 'com.isseiaoki:simplecropview:1.0.6'
}
```



## How does it work?

###Demo

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_basic_usage.gif)

###Code

**activity_main.xml**

```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:tools="http://schemas.android.com/tools"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:background="@color/base"
              android:orientation="vertical">

    <android.support.v7.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="64dp"
        android:layout_alignParentTop="true"
        >

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <TextView
                android:id="@+id/textTitle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentLeft="true"
                android:layout_centerVertical="true"
                android:text="SimpleCropView"
                android:textColor="@color/text"
                android:textSize="@dimen/text_size_s"
                />

            <Button
                android:id="@+id/crop_button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentRight="true"
                android:layout_centerVertical="true"
                android:layout_margin="16dp"
                android:text="CROP"
                android:background="@color/text"
                android:textSize="@dimen/text_size_s"
                android:textColor="@color/base"
                />

        </RelativeLayout>
    </android.support.v7.widget.Toolbar>

    <com.isseiaoki.simplecropview.CropImageView
        android:id="@+id/cropImageView"
        xmlns:custom="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="250dp"
        android:padding="16dp"
        custom:backgroundColor="@color/base"
        custom:handleColor="@color/handle"
        custom:guideColor="@color/guide"
        custom:overlayColor="@color/overlay"
        custom:frameColor="@color/frame"
        custom:cropMode="ratio_16_9"
        custom:frameStrokeWeight="1dp"
        custom:guideStrokeWeight="1dp"
        custom:guideShowMode="show_on_touch"
        custom:handleShowMode="show_always"
        custom:handleSize="8dp"
        custom:minFrameSize="100dp"
        custom:imgSrc="@mipmap/sample5"
        custom:touchPadding="8dp"
        />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="Cropped Image"
        android:textColor="@color/text"
        android:textSize="@dimen/text_size_s"
        android:layout_marginTop="24dp"
        android:layout_marginBottom="0dp"
        />


    <ImageView
        android:id="@+id/croppedImageView"
        android:layout_margin="16dp"
        android:layout_gravity="center"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:padding="0dp"
        />

</LinearLayout>
```



**MainActivity.java**

```
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
        final ImageView croppedImageView = (ImageView)findViewById(R.id.croppedImageView);
        final Button cropButton = (Button)findViewById(R.id.crop_button);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                croppedImageView.setImageBitmap(cropImageView.getCroppedBitmap());
            }
        });
    }
}
```


## Features
###CropMode

The option to adjust aspect ratio for cropping image.

```
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9);
```

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_crop_mode.gif)

>**Values:**
>```
RATIO_4_3, RATIO_3_4, RATIO_1_1, RATIO_16_9, RATIO_9_16, RATIO_FIT_IMAGE, RATIO_FREE
```

>`RATIO_FREE`: *This mode does not fix frame aspect ratio.*<br>
`RATIO_X_Y`: *Fixed aspect ratio mode. ratioX:ratioY = X:Y.*<br>
`RATIO_FIT_IMAGE`: *Fixed aspect ratio mode. ratioX:ratioY = (image width):(image height).*

>If you need other aspect ratio, use `setCustomRatio(int ratioX, int ratioY);`

###MinimumFrameSize
The SimpleCropView supports the minimum size of the image cropping frame in dp.<br>
It avoid that the image cropping frame size become smaller than the minimum size.

```Java
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setMinFrameSizeInDp(100);
```

![demo](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_minimum_frame_size.gif)

###CustomFrame
The SimpleCropView provides following attributes for customizing view appearance.

**Color**

```
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setBackgroundColor(getResources().getColor(android.R.color.black));
cropImageView.setOverlayColor(0xBBFFFFFF);
cropImageView.setFrameColor(0xFFFFFFFF);
cropImageView.setHandleColor(getResources().getColor(R.color.handle));
cropImageView.setGuideColor(getResources().getColor(android.R.color.black));
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/color-attributes.png)

**Stroke Weight & Handle Size**
```
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setFrameStrokeWeightInDp(3);
cropImageView.setGuideStrokeWeightInDp(1);
cropImageView.setHandleSizeInDp(getResources().getDimension(R.dimen.handle_size));
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/size-attributes.png)

**Handle Touch Padding**

If you want some additinal touch area for the image cropping frame handle, `setTouchPaddingInDp(int dp); ` may help you.
The handle's touch area is a circle radius R. (R = handle size + touch padding).
If the touch padding is 0, the touch area circle is the same as handle circle.

```
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setTouchPadding(16);
```

![](https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/handle-touch-padding.png)

**Handle & Guide ShowMode**

```
CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
cropImageView.setHandleShowMode(CropImageView.ShowMode.SHOW_ALWAYS);
cropImageView.setGuideShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
```

<table>
<tr>
<td><b>Handle ShowMode</b></td>
<td><b>Guide ShowMode</b></td>
<td><b>Appearance</b></td>
</tr>
<tr>
<td>SHOW_ALWAYS</td>
<td>SHOW_ALWAYS</td>
<td><img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/show_handle_and_guide.jpg" width="100%"></td>
</tr>
<tr>
<td>NOT_SHOW</td>
<td>NOT_SHOW</td>
<td><img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/not_show_handle_and_guide.jpg" width="100%"></td>
</tr>
<tr>
<td>SHOW_ALWAYS</td>
<td>NOT_SHOW</td>
<td><img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/graphic/show_handle_only.jpg" width="100%"></td>
</tr>
<tr>
<td>SHOW_ALWAYS</td>
<td>SHOW_ON_TOUCH</td>
<td><img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_guide_show_on_touch.gif" width="100%"></td>
</tr>
<tr>
<td>SHOW_ON_TOUCH</td>
<td>NOT_SHOW</td>
<td><img src="https://raw.github.com/wiki/IsseiAoki/SimpleCropView/images/gif/demo_handle_show_on_touch.gif" width="100%"></td>
</tr>
</table>

>**Values:**
>```
SHOW_ALWAYS, NOT_SHOW, SHOW_ON_TOUCH
```

##XML Attributes
XML code sample here.

```XML
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

<table>
	<tr>
		<td><center><b>XML Attribute</b></center></td>
		<td><center><b>Related Method</b></center></td>
		<td><center><b>Description</b></center></td>
	</tr>
	<tr>
		<td>custom:imgSrc</td>
		<td>setImageResource(int resId)<br>setImageBitmap(Bitmap bitmap)</td>
		<td>Set source image.</td>
	</tr>
	<tr>
		<td>custom:cropMode</td>
		<td>setCropMode(CropImageView.CropMode mode)</td>
		<td>Set crop mode.<i><br><br>
・whether to fix the aspect ratio of the image cropping frame
<br>・the aspect ratio of the image cropping frame for fixed-aspect-ratio</i></td>
	</tr>
		<tr>
		<td>custom:minFrameSize</td>
		<td>setMinFrameSizeInDp(int minDp)</td>
		<td>Set the image cropping frame minimum size in density-independent pixels.</td>
	</tr>
	<tr>
		<td>custom:backgroundColor</td>
		<td>setBackgroundColor(int bgColor)</td>
		<td>Set view background color.</td>
	</tr>
	<tr>
		<td>custom:overlayColor</td>
		<td>setOverlayColor(int overlayColor)</td>
		<td>Set image overlay color.</td>
	</tr>
	<tr>
		<td>custom:frameColor</td>
		<td>setFrameColor(int frameColor)</td>
		<td>Set the image cropping frame color.</td>
	</tr>
	<tr>
		<td>custom:handleColor</td>
		<td>setHandleColor(int frameColor)</td>
		<td>Set the handle color.</td>
	</tr>
	<tr>
		<td>custom:guideColor</td>
		<td>setGuideColor(int frameColor)</td>
		<td>Set the guide color.</td>
	</tr>
	<tr>
		<td>custom:handleSize</td>
		<td>setHandleSizeInDp(int handleDp)</td>
		<td>Set handle radius in density-independent pixels.</td>
	</tr>
	<tr>
		<td>custom:touchPadding</td>
		<td>setTouchPaddingInDp(int paddingDp)</td>
		<td>Set the image cropping frame handle touch padding(touch area) in density-independent pixels.<br><br><i>handle touch area : a circle of radius R.<br>(R = handle size + touch padding)</i></td>
	</tr>

	<tr>
		<td>custom:frameStrokeWeight</td>
		<td>setFrameStrokeWeightInDp(int weightDp)</td>
		<td>Set frame stroke weight in density-independent pixels.</td>
	</tr>
	<tr>
		<td>custom:guideStrokeWeight</td>
		<td>setGuideStrokeWeightInDp(int weightDp)</td>
		<td>Set guideline stroke weight in density-independent pixels.</td>
	</tr>
		<tr>
		<td>custom:guideShowMode</td>
		<td>setGuideShowMode(CropImageView.ShowMode mode)</td>
		<td>Set guideline show mode.<br> <i>(show_always/not_show/show_on_touch)</i></td>
	</tr>
	<tr>
		<td>custom:handleShowMode</td>
		<td>setHandleShowMode(CropImageView.ShowMode mode)</td>
		<td>Set handle show mode.<br><i>(show_always/not_show/show_on_touch)</i></td>
	</tr>
	<tr>
		<td>custom:cropEnabled</td>
		<td>setCropEnabled(boolean enabled)</td>
		<td>Set whether to show the image cropping frame.</td>
	</tr>
	
</table>


## License
All source code is licensed under the [MIT License](https://github.com/IsseiAoki/SimpleCropView/blob/master/LICENSE).

## Author
[IsseiAoki](https://github.com/IsseiAoki)

## Contact
i.greenwood.dev@gmail.com
