
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



#
#media related config for amlogic & 
#some dynamic shared libraries
#


#for amlogicplayer& liblayer related.
TARGET_WITH_AMLOGIC_EXTRATORS :=true
TARGET_WITH_AMLOGIC_SCREAN_MEDIASOURCE :=true
TARGET_WITH_AMLOGIC_RETRIEVER :=true
TARGET_WITH_AMLOGIC_PLAYERS :=true
#set on some prducts
BUILD_WITH_BOOT_PLAYER :=true

#########################################################################
#
#                                                PlayReady DRM
#
#########################################################################
ifeq ($(BOARD_PLAYREADY_LEVEL),1)
    BUILD_WITH_PLAYREADY_DRM := true
    BOARD_PLAYREADY_TVP := true
    TARGET_USE_OPTEEOS := true
    ifeq ($(BOARD_PLAYREADY_LP_IN_SS),)
        BOARD_PLAYREADY_LP_IN_SS := true
    endif
else
ifeq ($(BOARD_PLAYREADY_LEVEL), 3)
    BUILD_WITH_PLAYREADY_DRM := true
    ifeq ($(BOARD_PLAYREADY_LP_IN_SS),)
        BOARD_PLAYREADY_LP_IN_SS := true
    endif
endif
endif

ifeq ($(BUILD_WITH_PLAYREADY_DRM),true)

PRODUCT_PACKAGES += libdrmplayreadyplugin \
  libsmoothstreaming_test \
  libsmoothstreaming \
  libprwmv \
  libplayreadymediadrmplugin\
  playready \
  libdrmclientplayreadyplugin \
  9a04f079-9840-4286-ab92e65be0885f95

PRODUCT_COPY_FILES += \
    vendor/playready/playreadyplugin/keycert/zgpriv.dat:system/etc/drm/playready/zgpriv.dat \
    vendor/playready/playreadyplugin/keycert/bgroupcert.dat:system/etc/drm/playready/bgroupcert.dat \
    vendor/playready/playreadyplugin/keycert/devcerttemplate.dat:system/etc/drm/playready/devcerttemplate.dat \
    vendor/playready/playreadyplugin/keycert/priv.dat:system/etc/drm/playready/priv.dat

endif

#########################################################################
#
#                                     Verimatrix ViewRight Web
#
#########################################################################
ifeq ($(BUILD_WITH_VIEWRIGHT_WEB),true)

PRODUCT_PACKAGES += libVCASCommunication \

endif

#########################################################################
#
#                                     Verimatrix ViewRight Stb
#
#########################################################################
ifeq ($(BUILD_WITH_VIEWRIGHT_STB),true)

PRODUCT_PACKAGES += libvm_mod \


endif



PRODUCT_PACKAGES += ca-certificates.crt \
    libstagefright_wfd_sink




PRODUCT_PACKAGES += \
    libstagefright_soft_aacdec \
    libstagefright_soft_aacenc \
    libstagefright_soft_amrdec \
    libstagefright_soft_amrnbenc \
    libstagefright_soft_amrwbenc \
    libstagefright_soft_flacenc \
    libstagefright_soft_g711dec \
    libstagefright_soft_mp3dec \
    libstagefright_soft_mp2dec \
    libstagefright_soft_vorbisdec \
    libstagefright_soft_rawdec \
    libstagefright_soft_adpcmdec \
    libstagefright_soft_adifdec \
    libstagefright_soft_latmdec \
    libstagefright_soft_adtsdec \
    libstagefright_soft_alacdec \
    libstagefright_soft_dtshd \
    libstagefright_soft_apedec   \
    libstagefright_soft_wmaprodec \
    libstagefright_soft_wmadec    \
    libstagefright_soft_ddpdcv \


#soft codec related.
#
PRODUCT_PACKAGES += \
    libopenHEVC\
    libstagefright_soft_amh265dec\
    libstagefright_soft_amsoftdec\
    libstagefright_soft_amsoftadec \

#for drm widevine.
PRODUCT_PROPERTY_OVERRIDES += drm.service.enable=true
ifeq ($(BOARD_WIDEVINE_OEMCRYPTO_LEVEL),1)
    TARGET_USE_SECUREOS := true
    CONFIG_SECURE_OS_BDK := true
endif

ifeq ($(TARGET_USE_OPTEEOS), true)
    BOARD_OMX_WITH_OPTEE_TVP := true
else
ifeq ($(TARGET_USE_SECUREOS), true)
    BOARD_OMX_WITH_TVP := true
endif
endif

PRODUCT_PACKAGES += com.google.widevine.software.drm.xml \
    com.google.widevine.software.drm \
    libWVStreamControlAPI_L1 \
    libdrmwvmplugin_L1 \
    libwvm_L1 \
    libwvdrm_L1 \
    libWVStreamControlAPI_L3 \
    libdrmwvmplugin \
    libwvm \
    libwvdrm_L3 \
    libotzapi \
    libwvsecureos_api \
    libdrmdecrypt \
    libwvdrmengine \
    liboemcrypto \
    widevine \
    wvcenc \
    edef8ba9-79d6-4ace-a3c827dcd51d21ed \
    e043cde0-61d0-11e5-9c260002a5d5c51b

ifeq ($(TARGET_WITH_AMLOGIC_PLAYERS), true)
##player related
BUILD_WITH_AMLOGIC_PLAYER := true

PRODUCT_PACKAGES += libmedia_amlogic \
    librtmp \
    libmms_mod \
    libcurl_mod \
    libvhls_mod \
    libprhls_mod \
    libdash_mod.so  \

#audio
PRODUCT_PACKAGES += libamadec_omx_api \
    libfaad    \
    libape     \
    libmad     \
    libflac    \
    libcook    \
    libraac    \
    libamr     \
    libpcm     \
    libadpcm   \
    libpcm_wfd \
    libaac_helix \
    libamadec_wfd_out


endif

ifeq ($(TARGET_WITH_AMLOGIC_RETRIEVER), true)
#retriever
PRODUCT_PACKAGES += libamlogic_metadata_retriever
endif

ifeq ($(TARGET_WITH_AMLOGIC_SCREAN_MEDIASOURCE), true)
#for screensource
PRODUCT_PACKAGES += libstagefright_screenmediasource
endif


ifeq ($(TARGET_WITH_AMLOGIC_EXTRATORS), true)
#for ffmpeg extrator
PRODUCT_PACKAGES += libamffmpegadapter
#for other extrators
PRODUCT_PACKAGES += libstagefright_extrator
endif

ifeq ($(BUILD_WITH_BOOT_PLAYER),true)
PRODUCT_PACKAGES += bootplayer \
    libasound \
    alsalib-alsaconf \
    alsalib-pcmdefaultconf \
    alsalib-cardsaliasesconf

endif

