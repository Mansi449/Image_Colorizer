package mdg.com.slidermodule

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import mdg.com.slidermodule.asycn.ClipDrawableProcessorTask
import kotlinx.android.synthetic.main.slider_layout.view.*
import mdg.com.slidermodule.asycn.ReportDownloadState
import java.lang.Exception

/**
 * Created by Jemo on 12/5/16.
 */

class BeforeAfterSlider : RelativeLayout, ClipDrawableProcessorTask.OnAfterImageLoaded{

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val attr = context.theme.obtainStyledAttributes(attrs, R.styleable.BeforeAfterSlider,0,0)
        try {
            val thumbDrawable = attr.getDrawable(R.styleable.BeforeAfterSlider_slider_thumb)

            val beforeImage = attr.getDrawable(R.styleable.BeforeAfterSlider_before_image)
            val afterImageUrl = attr.getDrawable(R.styleable.BeforeAfterSlider_after_image)

            setSliderThumb(thumbDrawable)
            setBeforeImage(beforeImage)
            setAfterImage(afterImageUrl)
        }finally {
            attr.recycle()
        }
    }


    init {
        LayoutInflater.from(context).inflate(R.layout.slider_layout, this)
    }

    /**
     * set original image
     */
    fun setBeforeImage(imageUri: String): BeforeAfterSlider {
        //before_image_view_id.loadImage(imageUri)

        Glide.with(context).load(imageUri)
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception?, model: String?, target: com.bumptech.glide.request.target.Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        seekbar_id.visibility = View.VISIBLE
                        after_image_view_id.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(before_image_view_id)

        before_image_view_id.visibility = View.VISIBLE

        return this
    }

    fun setBeforeImage(imgDrawable: Drawable?): BeforeAfterSlider {
        before_image_view_id.loadImage(imgDrawable)
        return this
    }

    /**
     * set changed image
     */
    fun setAfterImage(imageUri: String) {
        ClipDrawableProcessorTask<String>(after_image_view_id, seekbar_id, context, this).execute(imageUri)
    }

    /**
     * set changed image
     */
    fun setAfterImage(imageDrawable: Drawable?) {
        ClipDrawableProcessorTask<Drawable>(after_image_view_id, seekbar_id, context, this).execute(imageDrawable)
    }

    /**
     * set thumb
     */
    fun setSliderThumb(thumb: Drawable?){
        thumb?.let {
            seekbar_id.thumb = thumb
        }
    }

    /**
     * fired up after second image loading will be finished
     */
    override fun onLoadedFinished(loadedSuccess: Boolean) {
        //seekbar_id.stayVisibleOrGone(loadedSuccess)
    }

}
