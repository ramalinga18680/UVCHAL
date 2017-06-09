/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * This class simulates a hardware JPEG compressor.  It receives image buffers
 * in RGBA_8888 format, processes them in a worker thread, and then pushes them
 * out to their destination stream.
 */

#ifndef HW_EMULATOR_CAMERA2_JPEG_H
#define HW_EMULATOR_CAMERA2_JPEG_H

#include "utils/Thread.h"
#include "utils/Mutex.h"
#include "utils/Timers.h"
#include "Base.h"
#include <hardware/camera3.h>
#include <utils/List.h>
#include <stdio.h>

extern "C" {
#include <jpeglib.h>
#include <jhead.h>
}

namespace android {
#define MAX_EXIF_TAGS_SUPPORTED 30
static const char TAG_MODEL[] = "Model";
static const char TAG_MAKE[] = "Make";
static const char TAG_FOCALLENGTH[] = "FocalLength";
static const char TAG_DATETIME[] = "DateTime";
static const char TAG_IMAGE_WIDTH[] = "ImageWidth";
static const char TAG_IMAGE_LENGTH[] = "ImageLength";
static const char TAG_GPS_LAT[] = "GPSLatitude";
static const char TAG_GPS_LAT_REF[] = "GPSLatitudeRef";
static const char TAG_GPS_LONG[] = "GPSLongitude";
static const char TAG_GPS_LONG_REF[] = "GPSLongitudeRef";
static const char TAG_GPS_ALT[] = "GPSAltitude";
static const char TAG_GPS_ALT_REF[] = "GPSAltitudeRef";
static const char TAG_GPS_MAP_DATUM[] = "GPSMapDatum";
static const char TAG_GPS_PROCESSING_METHOD[] = "GPSProcessingMethod";
static const char TAG_GPS_VERSION_ID[] = "GPSVersionID";
static const char TAG_GPS_TIMESTAMP[] = "GPSTimeStamp";
static const char TAG_GPS_DATESTAMP[] = "GPSDateStamp";
static const char TAG_ORIENTATION[] = "Orientation";

static const char TAG_EXPOSURETIME[] = "ExposureTime";
static const char TAG_APERTURE[] = "ApertureValue";
static const char TAG_FLASH[] = "Flash";
static const char TAG_WHITEBALANCE[] = "WhiteBalance";
static const char TAG_ISO_EQUIVALENT[] = "ISOSpeedRatings";
static const char TAG_DATETIME_DIGITIZED[] = "DateTimeDigitized";
static const char TAG_SUBSEC_TIME[] = "SubSecTime";
static const char TAG_SUBSEC_TIME_ORIG[] = "SubSecTimeOriginal";
static const char TAG_SUBSEC_TIME_DIG[] = "SubSecTimeDigitized";

struct CaptureRequest {
    uint32_t         frameNumber;
    camera3_stream_buffer *buf;
    Buffers         *sensorBuffers;
    bool    mNeedThumbnail;
};

class ExifElementsTable {
    public:
        ExifElementsTable() :
           gps_tag_count(0), exif_tag_count(0), position(0),
           jpeg_opened(false) { }
        ~ExifElementsTable();
        status_t insertElement(const char* tag, const char* value);
        void insertExifToJpeg(unsigned char* jpeg, size_t jpeg_size);
        status_t insertExifThumbnailImage(const char*, int);
        void saveJpeg(unsigned char* picture, size_t jpeg_size);
        static const char* degreesToExifOrientation(const char*);
        static void stringToRational(const char*, unsigned int*, unsigned int*);
        static bool isAsciiTag(const char* tag);
    private:
        ExifElement_t table[MAX_EXIF_TAGS_SUPPORTED];
        unsigned int gps_tag_count;
        unsigned int exif_tag_count;
        unsigned int position;
        bool jpeg_opened;
};

class JpegCompressor: private Thread, public virtual RefBase {
  public:

    JpegCompressor();
    ~JpegCompressor();

    struct JpegListener {
        // Called when JPEG compression has finished, or encountered an error
        virtual void onJpegDone(const StreamBuffer &jpegBuffer,
                bool success, CaptureRequest &r) = 0;
        // Called when the input buffer for JPEG is not needed any more,
        // if the buffer came from the framework.
        virtual void onJpegInputDone(const StreamBuffer &inputBuffer) = 0;
        virtual ~JpegListener();
    };

    // Start compressing COMPRESSED format buffers; JpegCompressor takes
    // ownership of the Buffers vector.
    status_t start();
    status_t setlistener(JpegListener *listener);
    void queueRequest(CaptureRequest &r);

    // Compress and block until buffer is complete.
    status_t compressSynchronous(Buffers *buffers);

    status_t cancel();

    bool isBusy();
    bool isStreamInUse(uint32_t id);

    bool waitForDone(nsecs_t timeout);
    ssize_t GetMaxJpegBufferSize();
    void SetMaxJpegBufferSize(ssize_t size);
    void SetExifInfo(struct ExifInfo info);
    int GenExif(ExifElementsTable* exiftable);

    // TODO: Measure this
    static const size_t kMaxJpegSize = 8000000;
    ssize_t mMaxbufsize;

  private:
    Mutex mBusyMutex;
    bool mIsBusy;
    Condition mDone;
    bool mSynchronous;

    Mutex mMutex;

    List<CaptureRequest*> mInJpegRequestQueue;
    Condition     mInJpegRequestSignal;
    camera3_stream_buffer *tempHalbuffers;
    Buffers         *tempBuffers;
    CaptureRequest mJpegRequest;
    bool mExitJpegThread;
    bool mNeedexif;
    int mMainJpegSize, mThumbJpegSize;
    uint8_t *mSrcThumbBuffer;
    uint8_t *mDstThumbBuffer;
    Buffers *mBuffers;
    int mPendingrequest;
    JpegListener *mListener;
    struct ExifInfo mInfo;
    StreamBuffer mJpegBuffer, mAuxBuffer;
    bool mFoundJpeg, mFoundAux;
    jpeg_compress_struct mCInfo;

    struct JpegError : public jpeg_error_mgr {
        JpegCompressor *parent;
    };
    j_common_ptr mJpegErrorInfo;

    struct JpegDestination : public jpeg_destination_mgr {
        JpegCompressor *parent;
    };

    bool checkError(const char *msg);
    status_t compress();

    status_t thumbcompress();
    void cleanUp();

    /**
     * Inherited Thread virtual overrides
     */
  private:
    virtual status_t readyToRun();
    virtual bool threadLoop();
};

} // namespace android

#endif
