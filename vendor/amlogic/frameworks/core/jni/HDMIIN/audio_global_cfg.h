#define CC_DISABLE_ALL_AUDIO_MODULE                         (0)

#if CC_DISABLE_ALL_AUDIO_MODULE == 1
    #define CC_DISABLE_ALSA_MODULE                          (1)
    #define CC_DISABLE_DSP_MODULE                           (1)
    #define CC_DISABLE_UTILS_MODULE                         (1)

    #if CC_DISABLE_DSP_MODULE == 1
        #define CC_DISABLE_AUDIO_EFFECT_MODULE              (1)
        #define CC_DISABLE_AUDIO_CONTROL_MODULE             (1)
    #else
        #define CC_DISABLE_AUDIO_EFFECT_MODULE              (0)
        #define CC_DISABLE_AUDIO_CONTROL_MODULE             (0)
    #endif

#else
    #define CC_DISABLE_ALSA_MODULE                          (0)
    #define CC_DISABLE_DSP_MODULE                           (0)
    #define CC_DISABLE_UTILS_MODULE                         (0)

    #if CC_DISABLE_DSP_MODULE == 1
        #define CC_DISABLE_AUDIO_EFFECT_MODULE              (1)
        #define CC_DISABLE_AUDIO_CONTROL_MODULE             (1)
    #else
        #define CC_DISABLE_AUDIO_EFFECT_MODULE              (0)
        #define CC_DISABLE_AUDIO_CONTROL_MODULE             (0)
    #endif

#endif

#define CC_EQ_HANDLED_BY_DSP                                (1)
#define CC_EQ_HANDLED_BY_AMPLIFIER                          (0)

#define	CC_ENABLE_EQ                                        (1)
