package xlk.paperless.standard.data;

import android.content.Context;
import android.os.Environment;

import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.FileUtil;

import static xlk.paperless.standard.data.Values.hasAllPermissions;
import static xlk.paperless.standard.data.Values.localPermission;


/**
 * @author xlk
 * @date 2020/3/9
 * @desc 全局静态常量类
 */
public class Constant {
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PaperlessStandardEdition/";
    public static final String INI_NAME = "client.ini";
    public static final String DEV_NAME = "client.dev";
    public static final String INI_FILE_PATH = ROOT_DIR + "/" + INI_NAME;
    public static final String DEV_FILE_PATH = ROOT_DIR + "/" + DEV_NAME;


    private static final String DIR_FILES = ROOT_DIR + "File/";
    public static final String dir_crash_log = ROOT_DIR + "Log/";
    public static final String dir_picture = DIR_FILES + "图片/";
    public static final String dir_data_file = DIR_FILES + "会议资料/";
    public static final String dir_annotation_file = DIR_FILES + "批注文件/";
    public static final String dir_export = DIR_FILES + "导出文件/";
    public static final String dir_cache = DIR_FILES + "临时文件/";

    //图片名称和下载标识

    public static final String MAIN_BG_PNG_TAG = "mian_bg";
    public static final String MAIN_LOGO_PNG_TAG = "mian_logo";
    public static final String SUB_BG_PNG_TAG = "sub_bg";
    public static final String ROOM_BG_PNG_TAG = "room_bg";
    public static final String NOTICE_BG_PNG_TAG = "notice_bg";
    public static final String NOTICE_LOGO_PNG_TAG = "notice_logo";
    /**
     * 会议发布文件
     */
    public static final String ADMIN_RELEASE_FILE = "release_file";

    //下载标识
    /**
     * 下载会议文件时的标识
     */
    public static final String download_meeting_file = "download_meeting_file";
    /**
     * 下载批注文件时的标识
     */
    public static final String download_annotation_file = "download_annotation_file";
    /**
     * 下载完成应该打开的文件标识
     */
    public static final String download_should_open_file = "download_should_open_file";
    /**
     * 会议议程的文件
     */
    public static final String download_agenda_file = "download_agenda_file";

    //上传文件时的标识

    public static final String upload_choose_file = "upload_choose_file";
    public static final String upload_draw_pic = "upload_draw_pic";
    public static final String upload_wps_file = "upload_wps_file";

    //Intent#putExtra的字符tag

    /**
     * 开始屏幕录制
     */
    public static final String action_screen_recording = "screen_recording";
    /**
     * 停止屏幕录制
     */
    public static final String action_stop_screen_recording = "stop_screen_recording";
    /**
     * 传入摄像头前置/后置
     */
    public static final String extra_camrea_type = "extra_camera_type";
    /**
     * 设备对讲
     */
    public static final String extra_inviteflag = "extra_inviteflag";
    public static final String extra_operdeviceid = "extra_operdeviceid";
    /**
     * 文件评分ID
     */
    public static final String extra_vote_id = "extra_vote_id";
    /**
     * 发起播放的类型
     */
    public static final String extra_video_action = "extra_video_action";
    /**
     * 发起播放的设备ID
     */
    public static final String extra_video_device_id = "extra_video_device_id";
    /**
     * 发起播放的文件类型
     */
    public static final String extra_video_subtype = "extra_video_subtype";
    /**
     * 采集类型
     */
    public static final String extra_collection_type = "extra_collection_type";
    /**
     * 公告ID
     */
    public static final String extra_bulletin_id = "extra_bulletin_id";
    /**
     * 登录用户id
     */
    public static final String extra_admin_id = "extra_admin_id";
    /**
     * 登录用户名称
     */
    public static final String extra_admin_name = "extra_admin_name";
    /**
     * 登录时的密码
     */
    public static final String extra_admin_password = "extra_admin_password";

    //EventBus自定义发送的Type

    private static final int BUS_BASE = 1000000;
    /**
     * logo图标下载完成
     */
    public static final int BUS_MAIN_LOGO = BUS_BASE + 1;
    /**
     * 主页背景图下载完成
     */
    public static final int BUS_MAIN_BG = BUS_BASE + 2;
    /**
     * 会议室背景图下载完成
     */
    public static final int BUS_ROOM_BG = BUS_BASE + 3;
    /**
     * 后台播放数据
     */
    public static final int BUS_VIDEO_DECODE = BUS_BASE + 4;
    /**
     * 后台播放数据
     */
    public static final int BUS_YUV_DISPLAY = BUS_BASE + 5;
    public static final int BUS_CLOSE_VIDEO = BUS_BASE + 6;
    /**
     * 议程文件下载完成
     */
    public static final int BUS_AGENDA_FILE = BUS_BASE + 7;
    /**
     * 添加图片通知
     */
    public static final int BUS_SHARE_PIC = BUS_BASE + 8;
    /**
     * 绘制截图
     */
    public static final int BUS_SCREEN_SHOT = BUS_BASE + 9;
    /**
     * 子界面北极图下载完成
     */
    public static final int BUS_SUB_BG = BUS_BASE + 10;
    /**
     * 后台下载公告logo完成
     */
    public static final int BUS_NOTICE_LOGO = BUS_BASE + 11;
    /**
     * 后台下载公告背景完成
     */
    public static final int BUS_NOTICE_BG = BUS_BASE + 12;
    /**
     * 后台收到强制性播放了
     */
    public static final int BUS_MANDATORY = BUS_BASE + 13;
    /**
     * 后台收到开始采集摄像头通知
     */
    public static final int BUS_COLLECT_CAMERA_START = BUS_BASE + 14;
    /**
     * 后台收到停止采集摄像头通知
     */
    public static final int BUS_COLLECT_CAMERA_STOP = BUS_BASE + 15;
    /**
     * 发送视频聊天的状态
     */
    public static final int BUS_CHAT_STATE = BUS_BASE + 16;
    /**
     * 网络状态改变
     */
    public static final int BUS_NET_WORK = BUS_BASE + 17;
    /**
     * 发送查看图片通知
     */
    public static final int BUS_PREVIEW_IMAGE = BUS_BASE + 18;
    /**
     * 通知是否注册WPS广播监听
     */
    public static final int BUS_WPS_RECEIVER = BUS_BASE + 19;
    /**
     * X5内核下加载完成
     */
    public static final int BUS_X5_INSTALL = BUS_BASE + 20;

    //会议功能码

    /**
     * 会议议程
     */
    public static final int fun_code_agenda_bulletin = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_AGENDA_BULLETIN_VALUE;
    /**
     * 会议资料
     */
    public static final int fun_code_meet_file = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_MATERIAL_VALUE;
    /**
     * 共享文件
     */
    public static final int fun_code_shared_file = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_SHAREDFILE_VALUE;
    /**
     * 批注文件
     */
    public static final int fun_code_postil_file = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_POSTIL_VALUE;
    /**
     * 会议交流
     */
    public static final int fun_code_message = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_MESSAGE_VALUE;
    /**
     * 视频直播
     */
    public static final int fun_code_video_stream = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_VIDEOSTREAM_VALUE;
    /**
     * 电子白板
     */
    public static final int fun_code_whiteboard = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_WHITEBOARD_VALUE;
    /**
     * 网页
     */
    public static final int fun_code_webbrowser = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_WEBBROWSER_VALUE;
    /**
     * 投票
     */
    public static final int fun_code_voteresult = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_VOTERESULT_VALUE;
    /**
     * 签到
     */
    public static final int fun_code_signinresult = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_SIGNINRESULT_VALUE;
    /**
     * 外部文档
     */
    public static final int fun_code_document = InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_DOCUMENT_VALUE;

    //资源ID

    public static final int resource_0 = 0;
    public static final int resource_1 = 1;
    public static final int resource_2 = 2;
    public static final int resource_3 = 3;
    public static final int resource_4 = 4;
    public static final int resource_10 = 10;
    public static final int resource_11 = 11;

    //固定的会议目录ID
    /**
     * 共享文件目录ID
     */
    public static final int SHARED_FILE_DIRECTORY_ID = 1;
    /**
     * 批注文件目录ID
     */
    public static final int ANNOTATION_FILE_DIRECTORY_ID = 2;
    /**
     * 会议资料目录ID
     */
    public static final int MEETDATA_FILE_DIRECTORY_ID = 3;

    //限制范围阀值
    /**
     * 标题文本最大字数
     */
    public static final int MAX_TITLE_LENGTH = 30;
    /**
     * 内容文本最大字数
     */
    public static final int MAX_CONTENT_LENGTH = 106;
    /**
     * 屏幕录制最低比特率
     */
    public static final int MINIMUM_BITRATE = 500;
    /**
     * 屏幕录制最大比特率
     */
    public static final int MAXIMUM_BITRATE = 10000;

    //自定义其它功能的功能码

    public static final int fun_code = 200000;
    public static final int fun_code_terminal = fun_code + 1;
    public static final int fun_code_vote = fun_code + 2;
    public static final int fun_code_election = fun_code + 3;
    public static final int fun_code_video = fun_code + 4;
    public static final int fun_code_screen = fun_code + 5;
    public static final int fun_code_bulletin = fun_code + 6;
    public static final int fun_code_score = fun_code + 7;

    /**
     * 投票时提交，用于签到参与投票
     */
    public static final int PB_VOTE_SELFLAG_CHECKIN = 0x80000000;

    //编码类型
    /**
     * VP8 video (i.e. video in .webm)
     */
    public static final String MIME_VIDEO_VP8 = "video/x-vnd.on2.vp8";
    /**
     * VP9 video (i.e. video in .webm)
     */
    public static final String MIME_VIDEO_VP9 = "video/x-vnd.on2.vp9";
    /**
     * SCREEN_HEIGHT.264/AVC video
     */
    public static final String MIME_VIDEO_AVC = "video/avc";
    /**
     * SCREEN_HEIGHT.265/HEVC video
     */
    public static final String MIME_VIDEO_HEVC = "video/hevc";
    /**
     * MPEG4 video
     */
    public static final String MIME_VIDEO_MPEG4 = "video/mp4v-es";

    //文件类别

    //  大类
    /**
     * 音频
     */
    public static final int MEDIA_FILE_TYPE_AUDIO = 0x00000000;
    /**
     * 视频
     */
    public static final int MEDIA_FILE_TYPE_VIDEO = 0x20000000;
    /**
     * 录制
     */
    public static final int MEDIA_FILE_TYPE_RECORD = 0x40000000;
    /**
     * 图片
     */
    public static final int MEDIA_FILE_TYPE_PICTURE = 0x60000000;
    /**
     * 升级
     */
    public static final int MEDIA_FILE_TYPE_UPDATE = 0xe0000000;
    /**
     * 临时文件
     */
    public static final int MEDIA_FILE_TYPE_TEMP = 0x80000000;
    /**
     * 其它文件
     */
    public static final int MEDIA_FILE_TYPE_OTHER = 0xa0000000;
    public static final int MAIN_TYPE_BITMASK = 0xe0000000;

    //  小类
    /**
     * PCM文件
     */
    public static final int MEDIA_FILE_TYPE_PCM = 0x01000000;
    /**
     * MP3文件
     */
    public static final int MEDIA_FILE_TYPE_MP3 = 0x02000000;
    /**
     * WAV文件
     */
    public static final int MEDIA_FILE_TYPE_ADPCM = 0x03000000;
    /**
     * FLAC文件
     */
    public static final int MEDIA_FILE_TYPE_FLAC = 0x04000000;
    /**
     * MP4文件
     */
    public static final int MEDIA_FILE_TYPE_MP4 = 0x07000000;
    /**
     * MKV文件
     */
    public static final int MEDIA_FILE_TYPE_MKV = 0x08000000;
    /**
     * RMVB文件
     */
    public static final int MEDIA_FILE_TYPE_RMVB = 0x09000000;
    /**
     * RM文件
     */
    public static final int MEDIA_FILE_TYPE_RM = 0x0a000000;
    /**
     * AVI文件
     */
    public static final int MEDIA_FILE_TYPE_AVI = 0x0b000000;
    /**
     * bmp文件
     */
    public static final int MEDIA_FILE_TYPE_BMP = 0x0c000000;
    /**
     * jpeg文件
     */
    public static final int MEDIA_FILE_TYPE_JPEG = 0x0d000000;
    /**
     * png文件
     */
    public static final int MEDIA_FILE_TYPE_PNG = 0x0e000000;
    /**
     * 其它文件
     */
    public static final int MEDIA_FILE_TYPE_OTHER_SUB = 0x10000000;

    public static final int SUB_TYPE_BITMASK = 0x1f000000;


    /* **** **  权限码  ** **** */
    /**
     * 同屏权限
     */
    public static final int permission_code_screen = 1;
    /**
     * 投影权限
     */
    public static final int permission_code_projection = 2;
    /**
     * 上传权限
     */
    public static final int permission_code_upload = 4;
    /**
     * 下载权限
     */
    public static final int permission_code_download = 8;
    /**
     * 投票权限
     */
    public static final int permission_code_vote = 16;

    /**
     * 判断本机是否拥有某权限
     */
    public static boolean hasPermission(int code) {
        if (hasAllPermissions) {
            return true;
        }
        return (localPermission & code) == code;
    }

    /**
     * 判断权限码是否有某一权限
     *
     * @param permission 权限
     * @param code       某一权限的权限码
     */
    public static boolean isHasPermission(int permission, int code) {
        return (permission & code) == code;
    }

    /**
     * 将权限码转换成数字集合 权限对应： 1 同屏权限, 2 投影权限, 3 上传权限, 4 下载权限, 5 投票权限
     *
     * @param permission 10进制int型数据
     */
    public static List<Integer> permission2list(int permission) {
        List<Integer> ls = new ArrayList<>();
        //将10进制转换成2进制字符串 010001
        String to2 = Integer.toBinaryString(permission);
        int length = to2.length();
        for (int j = 0; j < length; j++) {
            char c = to2.charAt(j);
            //将 char 转换成int型整数
            int a = c - '0';
            if (a == 1) {
                //从右往左数 <--
                //举个栗子： 000100  得到的是第3个
                int i1 = length - j;
                ls.add(i1);
            }
        }
        return ls;
    }

    /**
     * 判断是否是其它文件
     *
     * @param mediaid 文件ID
     */
    public static boolean isOther(int mediaid) {
        return (MAIN_TYPE_BITMASK & mediaid) == MEDIA_FILE_TYPE_OTHER;
    }

    /**
     * 判断是否是图片文件
     *
     * @param mediaid 文件ID
     */
    public static boolean isPicture(int mediaid) {
        return (MAIN_TYPE_BITMASK & mediaid) == MEDIA_FILE_TYPE_PICTURE;
    }

    /**
     * 判断是否是视频或音频文件
     *
     * @param mediaid 文件ID
     */
    public static boolean isVideo(int mediaid) {
        return (MAIN_TYPE_BITMASK & mediaid) == MEDIA_FILE_TYPE_VIDEO
                || (MAIN_TYPE_BITMASK & mediaid) == MEDIA_FILE_TYPE_AUDIO;
    }

    /**
     * 判断是否是文档类文件
     */
    public static boolean isDocument(int mediaid) {
        return !isPicture(mediaid) && !isVideo(mediaid) && !isOther(mediaid);
    }

    /**
     * 计算媒体ID
     *
     * @param path 文件路径
     * @return 媒体ID
     */
    public static int getMediaId(String path) {
        //其它
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return MEDIA_FILE_TYPE_OTHER | MEDIA_FILE_TYPE_OTHER_SUB;
        }
//        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
//            return MEDIA_FILE_TYPE_RECORD | MEDIA_FILE_TYPE_OTHER_SUB;
//        }
//        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
//            return MEDIA_FILE_TYPE_UPDATE | MEDIA_FILE_TYPE_OTHER_SUB;
//        }
//        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
//            return MEDIA_FILE_TYPE_TEMP | MEDIA_FILE_TYPE_OTHER_SUB;
//        }
//        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
//            return MAIN_TYPE_BITMASK | SUB_TYPE_BITMASK;
//        }
        //音频
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_PCM;
        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_MP3;
//        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_ADPCM;
//        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_FLAC;
//        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_MP4;
//        }
        //视屏
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_MKV;
        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_RMVB;
//        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_AVI;
//        }
//        if (FileUtil.isVideoFile(path)) {
//            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_RM;
//        }
        //图片
        if (FileUtil.isPictureFile(path)) {
            return MEDIA_FILE_TYPE_PICTURE | MEDIA_FILE_TYPE_BMP;
        }
//        if (FileUtil.isPictureFile(path)) {
//            return MEDIA_FILE_TYPE_PICTURE | MEDIA_FILE_TYPE_JPEG;
//        }
//        if (FileUtil.isPictureFile(path)) {
//            return MEDIA_FILE_TYPE_PICTURE | MEDIA_FILE_TYPE_PNG;
//        }
        return 0;
    }

    /**
     * 获取指定的MimeType
     *
     * @param codecId 后台回调ID
     * @return MimeType
     */
    public static String getMimeType(int codecId) {
        switch (codecId) {
            case 12:
                return MIME_VIDEO_MPEG4;
            case 13:
                return MIME_VIDEO_MPEG4;
            case 27:
                return MIME_VIDEO_AVC;
            case 28:
                return MIME_VIDEO_AVC;
            case 139:
                return MIME_VIDEO_VP8;
            case 140:
                return MIME_VIDEO_VP8;
            case 167:
                return MIME_VIDEO_VP9;
            case 168:
                return MIME_VIDEO_VP9;
            case 173:
                return MIME_VIDEO_HEVC;
            case 174:
                return MIME_VIDEO_HEVC;
            default:
                return MIME_VIDEO_AVC;
        }
    }

    /**
     * 判断设备是否是当前{@param type}类型
     *
     * @param type  类型
     * @param devId 设备ID
     * @see InterfaceMacro.Pb_DeviceIDType
     */
    public static boolean isThisDevType(int type, int devId) {
        return (devId & Constant.DEVICE_MEET_ID_MASK) == type;
    }

    public static final int DEVICE_MEET_ID_MASK = 0xfffc0000;

    /**
     * 获取该设备的类型名称
     *
     * @return 返回空表示是未识别的设备（服务器设备）
     */
    public static String getDeviceTypeName(Context context, int deviceId) {
        if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetDBServer_VALUE, deviceId)) {
            //会议数据库设备
            return context.getString(R.string.database_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetService_VALUE, deviceId)) {
            //会议茶水设备
            return context.getString(R.string.tea_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE, deviceId)) {
            //会议投影设备
            return context.getString(R.string.pro_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetCapture_VALUE, deviceId)) {
            //会议流采集设备
            return context.getString(R.string.capture_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetClient_VALUE, deviceId)) {
            //会议终端设备
            return context.getString(R.string.client_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetVideoClient_VALUE, deviceId)) {
            //会议视屏对讲客户端
            return context.getString(R.string.video_chat_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetPublish_VALUE, deviceId)) {
            //会议发布
            return context.getString(R.string.release_dev);
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DEVICE_MEET_PHPCLIENT_VALUE, deviceId)) {
            //PHP中转数据设备
        } else if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetShare_VALUE, deviceId)) {
            //会议一键同屏设备
            return context.getString(R.string.screen_dev);
        }
        return "";
    }

    /**
     * 获取操作界面名称
     */
    public static String getInterfaceStateName(Context context, int state) {
        if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace_VALUE) {
            return context.getString(R.string.face_main);
        } else if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE) {
            return context.getString(R.string.face_meet);
        } else if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_AdminFace_VALUE) {
            return context.getString(R.string.face_back);
        } else if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_VoteFace_VALUE) {
            return context.getString(R.string.face_vote);
        }
        return "";
    }

}
