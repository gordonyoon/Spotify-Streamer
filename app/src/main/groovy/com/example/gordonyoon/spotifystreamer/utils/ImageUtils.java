package com.example.gordonyoon.spotifystreamer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;


public abstract class ImageUtils {

    // use this image size for displaying the image in the list
    public static final int SMALL_IMAGE_SIZE_TARGET = 200;

    // use this image size for displaying in the now playing screen
    public static final int LARGE_IMAGE_SIZE_TARGET = 640;

    /**
     * Select the image from a list with the closest width to "target."
     *
     * @param images
     * @param target - pass in {@link ImageUtils#SMALL_IMAGE_SIZE_TARGET}
     *               or {@link ImageUtils#LARGE_IMAGE_SIZE_TARGET}
     * @return - may return null.
     */
    public static Image selectClosestImage(List<Image> images, int target) {
        Image closestImage;
        int closestImageDistToTarget;

        // find the distance to target of the first image
        if (images.size() > 0) {
            closestImage = images.get(0);
            closestImageDistToTarget = Math.abs(closestImage.width - target);
        } else {
            return null;
        }

        // find the image with the smallest distance to target
        for (int i = 1; i < images.size(); i++) {
            int distToTarget = Math.abs(images.get(i).width - target);
            if (distToTarget < closestImageDistToTarget) {
                closestImage = images.get(i);
            }
        }
        return closestImage;
    }
}
