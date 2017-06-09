#ifndef ANDROID_SERVERS_CAMERA_FINDAPK_H
#define ANDROID_SERVERS_CAMERA_FINDAPK_H
namespace android {
class List_Open
{
public:
    List_Open(char const* apk,char const*camera);
    ~List_Open();
    char*                apk_name;
    char*                camera_number;
    List_Open*           next;
};
class List_Or
{
public:
    List_Or(char const*apk,char const*pr,char const* cap);
    List_Or();
    ~List_Or();
    char*                apk_name;
    char*                pro;   //preview oritation
    char*                capo;  //capture oritation
    List_Or*             next;
};
class LoadXml
{
public:
    char*                getApkPackageName(int callingPid);
    int                  findApkCp(char * apk_name, List_Or* temp);
    bool                 findApkOp(char * apk_name);
    void                 print();
    int                  destroyList_Or(List_Or *head);
    int                  destroyList_Open(List_Open *head);
    void                 parseXMLFile();
    static void          StartElementHandlerWrapper(void *data,const char *name, const char **attrs);
    static void          EndElementHandlerWrapper(void *me, const char *name);
    void                 startElementHandler(const char *name, const char **attrs);
    void                 endElementHandler(const char *name);
                         LoadXml();
                         ~LoadXml();
private:
    char                 cameraCallProcess[64];
    int                  callingId;
    List_Open*           Lo_Head;
    List_Or*             Lr_Head;
    int                  indent;
};
}
#endif
