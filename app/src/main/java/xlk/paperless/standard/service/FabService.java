package xlk.paperless.standard.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.WmCanJoinMemberAdapter;
import xlk.paperless.standard.adapter.WmCanJoinProAdapter;
import xlk.paperless.standard.adapter.WmProjectorAdapter;
import xlk.paperless.standard.adapter.WmScreenMemberAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.ui.CustomBaseViewHolder;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.DialogUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.CameraActivity;
import xlk.paperless.standard.view.MyApplication;
import xlk.paperless.standard.view.chatonline.ChatVideoActivity;
import xlk.paperless.standard.view.draw.DrawActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static xlk.paperless.standard.view.MyApplication.mMediaProjection;
import static xlk.paperless.standard.view.MyApplication.screen_width;
import static xlk.paperless.standard.view.chatonline.ChatVideoActivity.isChatingOpened;
import static xlk.paperless.standard.view.draw.DrawActivity.isDrawing;

/**
 * @author xlk
 * @date 2020/3/23
 * @Description:
 */
public class FabService extends Service implements IFab {
    private final String TAG = "FabService-->";
    private JniHandler jni = JniHandler.getInstance();
    private WindowManager wm;
    private Context cxt;
    private long downTime, upTime;
    private int mTouchStartX, mTouchStartY;
    private WindowManager.LayoutParams mParams, defaultParams, postilParams, notParams;
    private ImageView hoverButton;
    private boolean hoverButtonIsShowing, menuViewIsShowing, serviceViewIsShowing, screenViewIsShowing,
            joinViewIsShowing, proViewIsShowing, voteViewIsShowing;
    private View menuView, serviceView, screenView, joinView, proView, voteView;
    private FabPresenter presenter;
    private int mScreenDensity;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    public static Bitmap screenShotBitmap = null;
    private WmScreenMemberAdapter memberAdapter;
    private WmProjectorAdapter projectorAdapter;
    private WmCanJoinMemberAdapter joinMemberAdapter;
    private WmCanJoinProAdapter joinProAdapter;
    private int currentVoteId;//当前收到的要参与的投票ID
    private int voteTimeouts;
    private int maxChooseCount = 1;//当前投票最多可以选择答案的个数
    private int currentChooseCount = 0;//当前投票已经选中的选项个数
    private View cameraView;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate -->");
        presenter = new FabPresenter(this, this);
        presenter.queryMember();
        presenter.register();
        showFab();
    }

    private void showFab() {
        initAdapter();
        LogUtil.d(TAG, "showFab -->");
        cxt = getApplicationContext();
        //获取 WindowManager
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(MyApplication.screen_width, MyApplication.screen_height, 0x1, 2);
        initHoverButton();
        initParams();
        hoverButtonIsShowing = true;
        wm.addView(hoverButton, mParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initHoverButton() {
        hoverButton = new ImageView(cxt);
        hoverButton.setTag("hoverButton");
        hoverButton.setImageResource(R.drawable.icon_fab);
        hoverButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downTime = System.currentTimeMillis();
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    int mx = rawX - mTouchStartX;
                    int my = rawY - mTouchStartY;
                    mParams.x += mx;
                    mParams.y += my;//相对于屏幕左上角的位置
                    wm.updateViewLayout(hoverButton, mParams);
                    mTouchStartX = rawX;
                    mTouchStartY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    upTime = System.currentTimeMillis();
                    if (upTime - downTime > 150) {
                        mParams.x = 0;
                        mParams.y = mTouchStartY - hoverButton.getHeight();
                        wm.updateViewLayout(hoverButton, mParams);
                    } else {
                        showMenuView();
                    }
                    break;
            }
            return true;
        });
    }

    private void initAdapter() {
        memberAdapter = new WmScreenMemberAdapter(R.layout.item_single_button, presenter.onLineMember);
        projectorAdapter = new WmProjectorAdapter(R.layout.item_single_button, presenter.onLineProjectors);
        joinMemberAdapter = new WmCanJoinMemberAdapter(R.layout.item_single_button, presenter.canJoinMembers);
        joinProAdapter = new WmCanJoinProAdapter(R.layout.item_single_button, presenter.canJoinPros);
    }

    @Override
    public void notifyOnLineAdapter() {
        if (memberAdapter != null) {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyChecks();
        }
        if (projectorAdapter != null) {
            projectorAdapter.notifyDataSetChanged();
            projectorAdapter.notifyChecks();
        }
    }

    @Override
    public void notifyJoinAdapter() {
        if (joinMemberAdapter != null) {
            joinMemberAdapter.notifyDataSetChanged();
            joinMemberAdapter.notifyChecks();
        }
        if (joinProAdapter != null) {
            joinProAdapter.notifyDataSetChanged();
            joinProAdapter.notifyChecks();
        }
    }

    private void initParams() {
        /** **** **  悬浮按钮  ** **** **/
        mParams = new WindowManager.LayoutParams();
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        setParamsType(mParams);
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        mParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        mParams.x = 0;
        mParams.y = screen_width - 100;
        mParams.windowAnimations = R.style.pop_Animation;
        /** **** **  弹框  ** **** **/
        defaultParams = new WindowManager.LayoutParams();
        setParamsType(defaultParams);
        defaultParams.format = PixelFormat.RGBA_8888;
        defaultParams.gravity = Gravity.CENTER;
//        defaultParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
//        defaultParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        defaultParams.width = MyApplication.screen_width / 2;
        defaultParams.height = MyApplication.screen_height / 2;
        defaultParams.windowAnimations = R.style.pop_Animation;
        /** **** **  充满屏幕  ** **** **/
        postilParams = new WindowManager.LayoutParams();
        setParamsType(postilParams);
        postilParams.format = PixelFormat.RGBA_8888;
        postilParams.gravity = Gravity.CENTER;
        postilParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        postilParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        postilParams.windowAnimations = R.style.pop_Animation;
        /** **** **  外部不可点击  ** **** **/
        notParams = new WindowManager.LayoutParams();
        setParamsType(notParams);
        notParams.format = PixelFormat.RGBA_8888;
        notParams.gravity = Gravity.CENTER;
        notParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        notParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        notParams.windowAnimations = R.style.pop_Animation;
    }

    private void setParamsType(WindowManager.LayoutParams params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;//总是出现在应用程序窗口之上
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//总是出现在应用程序窗口之上
        }
    }

    //菜单视图
    private void showMenuView() {
        menuView = LayoutInflater.from(cxt).inflate(R.layout.wm_menu_view, null);
        menuView.setTag("menuView");
        CustomBaseViewHolder.MenuViewHolder menuViewHolder = new CustomBaseViewHolder.MenuViewHolder(menuView);
        menuViewHolderEvent(menuViewHolder);
        showPop(hoverButton, menuView);
    }

    //菜单视图事件
    private void menuViewHolderEvent(CustomBaseViewHolder.MenuViewHolder holder) {
        holder.wm_menu_back.setOnClickListener(v -> showPop(menuView, hoverButton, mParams));
        //截图批注
        holder.wm_menu_screenshot.setOnClickListener(v -> screenshot());
        //呼叫服务
        holder.wm_menu_service.setOnClickListener(v -> {
            showServiceView();
        });
        //发起同屏
        holder.wm_menu_start_screen.setOnClickListener(v -> {
            if (Constant.hasPermission(1)) {
                showScreenView(1);
            } else {
                ToastUtil.show(cxt, R.string.err_NoPermission);
            }
        });
        //结束同屏
        holder.wm_menu_stop_screen.setOnClickListener(v -> {
            if (Constant.hasPermission(1)) {
                showScreenView(2);
            } else {
                ToastUtil.show(cxt, R.string.err_NoPermission);
            }
        });
        //加入同屏
        holder.wm_menu_join_screen.setOnClickListener(v -> {
            if (Constant.hasPermission(1)) {
                presenter.queryCanJoin();
                showJoinView();
            } else {
                ToastUtil.show(cxt, R.string.err_NoPermission);
            }
        });
        //发起投影
        holder.wm_menu_start_projection.setOnClickListener(v -> {
            if (Constant.hasPermission(2)) {
                showProView(1);
            } else {
                ToastUtil.show(cxt, R.string.err_NoPermission);
            }
        });
        //结束投影
        holder.wm_menu_stop_projection.setOnClickListener(v -> {
            if (Constant.hasPermission(2)) {
                showProView(2);
            } else {
                ToastUtil.show(cxt, R.string.err_NoPermission);
            }
        });
    }

    //投影视图
    private void showProView(int type) {
        proView = LayoutInflater.from(cxt).inflate(R.layout.wm_pro_view, null);
        proView.setTag("proView");
        CustomBaseViewHolder.ProViewHolder proViewHolder = new CustomBaseViewHolder.ProViewHolder(proView);
        proViewHolderEvent(proViewHolder, type);
        showPop(menuView, proView);
    }

    private void proViewHolderEvent(CustomBaseViewHolder.ProViewHolder holder, int type) {
        holder.wm_pro_mandatory.setVisibility(type == 1 ? View.VISIBLE : View.INVISIBLE);
        holder.wm_pro_title.setText(type == 1 ? cxt.getString(R.string.launch_pro_title) : cxt.getString(R.string.stop_pro_title));
        holder.wm_pro_launch_pro.setText(type == 1 ? cxt.getString(R.string.launch_pro) : cxt.getString(R.string.stop_pro));
        holder.wm_pro_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_pro_rv.setAdapter(projectorAdapter);
        projectorAdapter.setOnItemClickListener((adapter, view, position) -> {
            projectorAdapter.choose(presenter.onLineProjectors.get(position).getDevcieid());
            holder.wm_pro_all.setChecked(projectorAdapter.isChooseAll());
        });
        holder.wm_pro_all.setOnClickListener(v -> {
            boolean checked = holder.wm_pro_all.isChecked();
            holder.wm_pro_all.setChecked(checked);
            projectorAdapter.setChooseAll(checked);
        });
        holder.wm_pro_launch_pro.setOnClickListener(v -> {
            List<Integer> ids = projectorAdapter.getChooseIds();
            if (ids.isEmpty()) {
                ToastUtil.show(cxt, cxt.getString(R.string.please_choose_projector_first));
                return;
            }
            boolean checked = holder.wm_pro_full.isChecked();
            List<Integer> res = new ArrayList<>();
            if (checked) {
                res.add(0);
            } else {
                if (holder.wm_pro_flow1.isChecked()) res.add(1);
                if (holder.wm_pro_flow2.isChecked()) res.add(2);
                if (holder.wm_pro_flow3.isChecked()) res.add(3);
                if (holder.wm_pro_flow4.isChecked()) res.add(4);
            }
            if (res.isEmpty()) {
                ToastUtil.show(cxt, cxt.getString(R.string.please_choose_res_first));
                return;
            }
            if (type == 1) {//发起投影
                boolean isMandatory = holder.wm_pro_mandatory.isChecked();
                int triggeruserval = isMandatory ? InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE
                        : InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_ZERO_VALUE;
                jni.streamPlay(MyApplication.localDeviceId, 2, triggeruserval, res, ids);
            } else {//结束投影
                jni.stopResourceOperate(res, ids);
            }
            showPop(proView, hoverButton, mParams);
        });
        holder.wm_pro_cancel.setOnClickListener(v -> showPop(proView, hoverButton, mParams));
    }

    //加入同屏视图
    private void showJoinView() {
        joinView = LayoutInflater.from(cxt).inflate(R.layout.wm_screen_view, null);
        joinView.setTag("joinView");
        CustomBaseViewHolder.ScreenViewHolder joinViewHolder = new CustomBaseViewHolder.ScreenViewHolder(joinView);
        joinViewHolderEvent(joinViewHolder);
        showPop(menuView, joinView);
    }

    //加入同屏视图事件
    private void joinViewHolderEvent(CustomBaseViewHolder.ScreenViewHolder holder) {
        holder.wm_screen_mandatory.setVisibility(View.INVISIBLE);
        holder.wm_screen_cb_attendee.setVisibility(View.INVISIBLE);
        holder.wm_screen_cb_projector.setVisibility(View.INVISIBLE);
        holder.wm_screen_title.setText(cxt.getString(R.string.choose_join_screen));
        holder.wm_screen_launch.setText(cxt.getString(R.string.join_screen));
        holder.wm_screen_rv_attendee.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_screen_rv_attendee.setAdapter(joinMemberAdapter);
        joinMemberAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                joinMemberAdapter.choose(presenter.canJoinMembers.get(position).getDevceid());
            }
        });
        holder.wm_screen_rv_projector.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_screen_rv_projector.setAdapter(joinProAdapter);
        joinProAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                joinProAdapter.choose(presenter.canJoinPros.get(position).getResPlay().getDevceid());
            }
        });
        holder.wm_screen_cancel.setOnClickListener(v -> showPop(joinView, hoverButton, mParams));
        //加入同屏
        holder.wm_screen_launch.setOnClickListener(v -> {
            List<Integer> ids = new ArrayList<>();
            int chooseId = joinMemberAdapter.getChooseId();
            if (chooseId != -1) {
                ids.add(chooseId);
            }
            int chooseId1 = joinProAdapter.getChooseId();
            if (chooseId1 != -1) {
                ids.add(chooseId1);
            }
            if (ids.size() > 1) {
                ToastUtil.show(cxt, R.string.can_only_choose_one);
            } else if (ids.isEmpty()) {
                ToastUtil.show(cxt, R.string.err_target_NotNull);
            } else {
                List<Integer> res = new ArrayList<>();
                res.add(0);
                List<Integer> devs = new ArrayList<>();
                devs.add(MyApplication.localDeviceId);
                jni.streamPlay(ids.get(0), 2, 0, res, devs);
                showPop(joinView, hoverButton, mParams);
            }
        });
    }

    //服务视图
    private void showServiceView() {
        serviceView = LayoutInflater.from(cxt).inflate(R.layout.wm_service_view, null);
        serviceView.setTag("serviceView");
        CustomBaseViewHolder.ServiceViewHolder serviceViewHolder = new CustomBaseViewHolder.ServiceViewHolder(serviceView);
        serviceViewHolderEvent(serviceViewHolder);
        showPop(menuView, serviceView);
    }

    //服务视图事件
    private void serviceViewHolderEvent(CustomBaseViewHolder.ServiceViewHolder holder) {
        holder.wm_service_close.setOnClickListener(v -> showPop(serviceView, hoverButton, mParams));
        holder.wm_service_pager.setOnClickListener(v -> holder.wm_service_edt.setText(cxt.getResources().getString(R.string.service_pager)));
        holder.wm_service_pen.setOnClickListener(v -> holder.wm_service_edt.setText(cxt.getResources().getString(R.string.service_pen)));
        holder.wm_service_tea.setOnClickListener(v -> holder.wm_service_edt.setText(cxt.getResources().getString(R.string.service_tea)));
        holder.wm_service_calculate.setOnClickListener(v -> holder.wm_service_edt.setText(cxt.getResources().getString(R.string.service_calculate)));
        holder.wm_service_waiter.setOnClickListener(v -> holder.wm_service_edt.setText(cxt.getResources().getString(R.string.service_waiter)));
        holder.wm_service_clean.setOnClickListener(v -> holder.wm_service_edt.setText(cxt.getResources().getString(R.string.service_clean)));
        holder.wm_service_send.setOnClickListener(v -> {
            String msg = holder.wm_service_edt.getText().toString().trim();
            if (!msg.isEmpty()) {
                List<Integer> arr = new ArrayList<>();
                arr.add(0);//会议服务类请求则为 0
                jni.sendChatMessage(msg, InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Other_VALUE, arr);
            }
        });
    }

    //同屏视图 type =1发起同屏，=2结束同屏
    private void showScreenView(int type) {
        //同屏视图
        screenView = LayoutInflater.from(cxt).inflate(R.layout.wm_screen_view, null);
        screenView.setTag("screenView");
        CustomBaseViewHolder.ScreenViewHolder screenViewHolder = new CustomBaseViewHolder.ScreenViewHolder(screenView);
        screenViewHolderEvent(screenViewHolder, type);
        showPop(menuView, screenView);
    }

    //同屏视图事件
    private void screenViewHolderEvent(CustomBaseViewHolder.ScreenViewHolder holder, int type) {
        holder.wm_screen_mandatory.setVisibility((type == 1) ? View.VISIBLE : View.INVISIBLE);
        if (type == 1) {
            holder.wm_screen_launch.setText(cxt.getString(R.string.launch_screen));
            holder.wm_screen_title.setText(cxt.getString(R.string.launch_screen_title));
        } else if (type == 2) {
            holder.wm_screen_launch.setText(cxt.getString(R.string.stop_screen));
            holder.wm_screen_title.setText(cxt.getString(R.string.stop_screen_title));
        }
        holder.wm_screen_cancel.setOnClickListener(v -> showPop(screenView, hoverButton, mParams));
        holder.wm_screen_rv_attendee.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_screen_rv_attendee.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListener((adapter, view, position) -> {
            memberAdapter.choose(presenter.onLineMember.get(position).getDeviceDetailInfo().getDevcieid());
            holder.wm_screen_cb_attendee.setChecked(memberAdapter.isChooseAll());
        });
        holder.wm_screen_cb_attendee.setOnClickListener(v -> {
            boolean checked = holder.wm_screen_cb_attendee.isChecked();
            holder.wm_screen_cb_attendee.setChecked(checked);
            memberAdapter.setChooseAll(checked);
        });
        holder.wm_screen_rv_projector.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_screen_rv_projector.setAdapter(projectorAdapter);
        projectorAdapter.setOnItemClickListener((adapter, view, position) -> {
            projectorAdapter.choose(presenter.onLineProjectors.get(position).getDevcieid());
            holder.wm_screen_cb_projector.setChecked(projectorAdapter.isChooseAll());
        });
        holder.wm_screen_cb_projector.setOnClickListener(v -> {
            boolean checked = holder.wm_screen_cb_projector.isChecked();
            holder.wm_screen_cb_projector.setChecked(checked);
            projectorAdapter.setChooseAll(checked);
        });
        //发起/结束同屏
        holder.wm_screen_launch.setOnClickListener(v -> {
            List<Integer> ids = memberAdapter.getChooseIds();
            ids.addAll(projectorAdapter.getChooseIds());
            if (ids.isEmpty()) {
                ToastUtil.show(cxt, R.string.err_target_NotNull);
            } else {
                List<Integer> temps = new ArrayList<>();
                temps.add(0);
                if (type == 1) {//发起同屏
                    int triggeruserval = 0;
                    if (holder.wm_screen_mandatory.isChecked()) {//是否强制同屏
                        triggeruserval = InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
                    }
                    jni.streamPlay(MyApplication.localDeviceId, 2, triggeruserval, temps, ids);
                } else {//结束同屏
                    jni.stopResourceOperate(temps, ids);
                }
                showPop(screenView, hoverButton, mParams);
            }
        });

    }

    boolean dialogIsShowing = false;
    private LinkedList<InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify> permissionsRequests = new LinkedList<>();//存放收到的申请权限信息

    @Override
    public void applyPermissionsInform(InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify info) {
        if (dialogIsShowing) {
            permissionsRequests.addLast(info);
            return;
        }
        dialogIsShowing = true;
        int deviceid = info.getDeviceid();
        int memberid = info.getMemberid();
        int privilege = info.getPrivilege();
        DialogUtil.createDialog(cxt, cxt.getString(R.string.apply_permissions, presenter.getMemberNameById(memberid)),
                cxt.getString(R.string.agree), cxt.getString(R.string.reject), new DialogUtil.onDialogClickListener() {
                    @Override
                    public void positive(DialogInterface dialog) {
                        jni.revertAttendPermissionsRequest(deviceid, 1);
                        dialog.dismiss();
                    }

                    @Override
                    public void negative(DialogInterface dialog) {
                        jni.revertAttendPermissionsRequest(deviceid, 0);
                        dialog.dismiss();
                    }

                    @Override
                    public void dismiss(DialogInterface dialog) {
                        dialogIsShowing = false;
                        if (!permissionsRequests.isEmpty()) {
                            InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify item = permissionsRequests.removeFirst();
                            if (item != null) {
                                LogUtil.d(TAG, "dismiss -->" + "处理了一个还有下一个申请要处理");
                                applyPermissionsInform(item);
                            }
                        }
                    }
                });
    }

    //收到对讲通知
    @Override
    public void showView(int inviteflag, int operdeviceid) {
        boolean isAsk = (inviteflag & InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_ASK_VALUE) == InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_ASK_VALUE;
        if (!isAsk) {
            if (!isChatingOpened) {
                startActivity(new Intent(this, ChatVideoActivity.class)
                        .putExtra(Constant.extra_inviteflag, inviteflag)
                        .putExtra(Constant.extra_operdeviceid, operdeviceid)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            } else {
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_CHAT_STATE).objs(inviteflag, operdeviceid).build());
            }
//            showOpenCamera(inviteflag, operdeviceid);
            //强制的
            int flag = inviteflag | InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_DEAL_VALUE;
            LogUtil.d(TAG, "强制的则直接同意：" + flag);
            jni.replyDeviceIntercom(operdeviceid, flag);
            return;
        }
        DialogUtil.createDialog(cxt, getString(R.string.deviceIntercom_Inform_title, presenter.getMemberNameByDevid(operdeviceid)),
                getString(R.string.agree), getString(R.string.reject), new DialogUtil.onDialogClickListener() {
                    @Override
                    public void positive(DialogInterface dialog) {
                        if (!isChatingOpened) {
                            startActivity(new Intent(cxt, ChatVideoActivity.class)
                                    .putExtra(Constant.extra_inviteflag, inviteflag)
                                    .putExtra(Constant.extra_operdeviceid, operdeviceid)
                                    .setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        } else {
                            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_CHAT_STATE).objs(inviteflag, operdeviceid).build());
                        }
//                        showOpenCamera(inviteflag, operdeviceid);
                        int flag = inviteflag | InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_DEAL_VALUE;
                        LogUtil.d(TAG, "showView -->" + "同意：" + flag);
                        jni.replyDeviceIntercom(operdeviceid, flag);
                        dialog.dismiss();
                    }

                    @Override
                    public void negative(DialogInterface dialog) {
                        LogUtil.d(TAG, "showView -->" + "拒绝：" + inviteflag);
                        jni.replyDeviceIntercom(operdeviceid, inviteflag);
                        dialog.dismiss();
                    }

                    @Override
                    public void dismiss(DialogInterface dialog) {

                    }
                });
    }

    //打开摄像头视图
    @Override
    public void showOpenCamera(int inviteflag, int operdeviceid) {
        cameraView = LayoutInflater.from(cxt).inflate(R.layout.wm_camera_choose, null);
        cameraView.findViewById(R.id.wm_camera_pre).setOnClickListener(v -> {
            if (AppUtil.checkCamera(cxt, 1)) {
                Intent intent = new Intent(cxt, CameraActivity.class);
                intent.putExtra(Constant.extra_camrea_type, 1);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                cxt.startActivity(intent);
                wm.removeView(cameraView);
                int flag = inviteflag | InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_DEAL_VALUE;
                LogUtil.d(TAG, "强制的则直接同意：" + flag);
                jni.replyDeviceIntercom(operdeviceid, flag);
            } else {
                ToastUtil.show(cxt, R.string.no_camera_1);
            }
        });
        cameraView.findViewById(R.id.wm_camera_back).setOnClickListener(v -> {
            if (AppUtil.checkCamera(cxt, 0)) {
                Intent intent = new Intent(cxt, CameraActivity.class);
                intent.putExtra(Constant.extra_camrea_type, 0);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                cxt.startActivity(intent);
                wm.removeView(cameraView);
                int flag = inviteflag | InterfaceDevice.Pb_DeviceInviteFlag.Pb_DEVICE_INVITECHAT_FLAG_DEAL_VALUE;
                LogUtil.d(TAG, "强制的则直接同意：" + flag);
                jni.replyDeviceIntercom(operdeviceid, flag);
            } else {
                ToastUtil.show(cxt, R.string.no_camera_0);
            }
        });
        cameraView.findViewById(R.id.wm_camera_reject).setOnClickListener(v -> {
            wm.removeView(cameraView);
        });
        wm.addView(cameraView, notParams);
    }

    @Override
    public void closeVoteView() {
        if (voteViewIsShowing) {
            wm.removeView(voteView);
            voteViewIsShowing = false;
            currentVoteId = -1;
            currentChooseCount = 0;
        }
    }

    //投票视图
    public void showVoteView(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo info) {
        if (voteViewIsShowing) {
            LogUtil.e(TAG, "showVoteView -->" + "已经有投票正在展示中");
            return;
        }
        currentVoteId = info.getVoteid();
        voteView = LayoutInflater.from(cxt).inflate(R.layout.wm_vote_view, null);
        voteView.setTag("voteView");
        CustomBaseViewHolder.VoteViewHolder voteViewHolder = new CustomBaseViewHolder.VoteViewHolder(voteView);
        voteViewHolderEvent(voteViewHolder, info);
        voteViewIsShowing = true;
        wm.addView(voteView, postilParams);
    }

    //投票视图事件
    private void voteViewHolderEvent(CustomBaseViewHolder.VoteViewHolder holder, InterfaceVote.pbui_Item_MeetOnVotingDetailInfo info) {
        voteTimeouts = info.getTimeouts();
        String voteMode = getVoteMode(info);
        holder.wm_vote_title.setText(info.getContent().toStringUtf8());
        holder.wm_vote_type.setText(voteMode);
        initCheckBox(holder, info);
        int selectItem = 0 | Constant.PB_VOTE_SELFLAG_CHECKIN;
        jni.submitVoteResult(1, currentVoteId, selectItem);
        if (voteTimeouts <= 0) {
            holder.wm_vote_chronometer.setVisibility(View.GONE);
        } else {
            holder.wm_vote_chronometer.setVisibility(View.VISIBLE);
            holder.wm_vote_chronometer.setBase(SystemClock.elapsedRealtime());
            holder.wm_vote_chronometer.start();
            holder.wm_vote_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    voteTimeouts--;
                    if (voteTimeouts <= 0) {
                        chronometer.stop();
                        closeVoteView();
                    } else {
                        String str = DateUtil.formatSeconds(voteTimeouts);
                        chronometer.setText(str);
                    }
                }
            });
        }
        chooseEvent(holder.checkBox1);
        chooseEvent(holder.checkBox2);
        chooseEvent(holder.checkBox3);
        chooseEvent(holder.checkBox4);
        chooseEvent(holder.checkBox5);
        holder.wm_vote_submit.setOnClickListener(v -> {
            int answer = 0;
            if (holder.checkBox1.isChecked()) answer += 1;
            if (holder.checkBox2.isChecked()) answer += 2;
            if (holder.checkBox3.isChecked()) answer += 4;
            if (holder.checkBox4.isChecked()) answer += 8;
            if (holder.checkBox5.isChecked()) answer += 16;
            if (answer != 0) {
                jni.submitVoteResult(info.getSelectcount(), currentVoteId, answer);
                closeVoteView();
            } else {
                ToastUtil.show(cxt, R.string.please_choose_answer_first);
            }
        });
    }

    private void chooseEvent(CheckBox checkBox) {
        checkBox.setOnClickListener(v -> {
            boolean checked = checkBox.isChecked();
            if (checked) {//将要设置成选中
                if (currentChooseCount < maxChooseCount) {
                    currentChooseCount++;
                    checkBox.setChecked(checked);
                } else {
                    checkBox.setChecked(!checked);
                    ToastUtil.show(cxt, cxt.getString(R.string.max_choose_, String.valueOf(maxChooseCount)));
                }
            } else {
                currentChooseCount--;
                checkBox.setChecked(checked);
            }
        });
    }

    private String getVoteMode(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo info) {
        String voteMode = "(";
        switch (info.getType()) {
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE://单选
                voteMode += cxt.getString(R.string.type_single) + "，";
                maxChooseCount = 1;
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE://5选4
                voteMode += cxt.getString(R.string.type_4_5) + "，";
                maxChooseCount = 4;
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE:
                voteMode += cxt.getString(R.string.type_3_5) + "，";
                maxChooseCount = 3;
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE:
                voteMode += cxt.getString(R.string.type_2_5) + "，";
                maxChooseCount = 2;
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE:
                voteMode += cxt.getString(R.string.type_2_3) + "，";
                maxChooseCount = 2;
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_MANY_VALUE:
                voteMode += cxt.getString(R.string.type_multi) + "，";
                maxChooseCount = info.getSelectcount() - 1;
                break;
        }
        if (info.getMode() == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE) {//匿名
            voteMode += cxt.getString(R.string.mode_anonymous);
        } else {
            voteMode += cxt.getString(R.string.mode_register);
        }
        voteMode += "）";
        return voteMode;
    }

    private void initCheckBox(CustomBaseViewHolder.VoteViewHolder holder, InterfaceVote.pbui_Item_MeetOnVotingDetailInfo info) {
        holder.checkBox1.setVisibility(View.VISIBLE);
        holder.checkBox2.setVisibility(View.VISIBLE);
        holder.checkBox3.setVisibility(View.VISIBLE);
        holder.checkBox4.setVisibility(View.VISIBLE);
        holder.checkBox5.setVisibility(View.VISIBLE);
        int selectcount = info.getSelectcount();//有效选项
        switch (selectcount) {
            case 2:
                holder.checkBox3.setVisibility(View.GONE);
                holder.checkBox4.setVisibility(View.GONE);
                holder.checkBox5.setVisibility(View.GONE);
                break;
            case 3:
                holder.checkBox4.setVisibility(View.GONE);
                holder.checkBox5.setVisibility(View.GONE);
                break;
            case 4:
                holder.checkBox5.setVisibility(View.GONE);
                break;
        }
        List<ByteString> textList = info.getTextList();
        for (int i = 0; i < textList.size(); i++) {
            String s = textList.get(i).toStringUtf8();
            switch (i) {
                case 0:
                    holder.checkBox1.setText(s);
                    break;
                case 1:
                    holder.checkBox2.setText(s);
                    break;
                case 2:
                    holder.checkBox3.setText(s);
                    break;
                case 3:
                    holder.checkBox4.setText(s);
                    break;
                case 4:
                    holder.checkBox5.setText(s);
                    break;
            }
        }
    }

    private void screenshot() {
        delAllView();
        new Handler().postDelayed(() -> {
            startVirtual();
            new Handler().postDelayed(() -> {
                startScreen();
            }, 100);
        }, 500);
    }

    private void startVirtual() {
        try {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    MyApplication.screen_width, MyApplication.screen_height, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            LogUtil.d(TAG, "virtual displayed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startScreen() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            LogUtil.e(TAG, "startScreen :  image 为null --> ");
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        LogUtil.d(TAG, "image data captured bitmap是否为空：" + (bitmap == null));
        //截图完毕，从新显示悬浮按钮
        hoverButtonIsShowing = true;
        wm.addView(hoverButton, mParams);
        if (bitmap != null) {
            screenShotBitmap = bitmap;
            if (isDrawing) {
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SCREEN_SHOT).build());
            } else {
                Intent intent = new Intent(cxt, DrawActivity.class);
                startActivity(intent);
            }
        }
    }

    /**
     * 展示新的弹框
     *
     * @param removeView 正在展示的view
     * @param addView    需要替换的view
     * @param params     params配置
     */
    private void showPop(View removeView, View addView, WindowManager.LayoutParams params) {
        wm.removeView(removeView);
        wm.addView(addView, params);
        setIsShowing(removeView, addView);
    }

    private void showPop(View removeView, View addView) {
        wm.removeView(removeView);
        wm.addView(addView, defaultParams);
        setIsShowing(removeView, addView);
    }

    private void delAllView() {
        if (hoverButtonIsShowing) wm.removeView(hoverButton);
        if (menuViewIsShowing) wm.removeView(menuView);
        if (serviceViewIsShowing) wm.removeView(serviceView);
        if (screenViewIsShowing) wm.removeView(screenView);
        if (joinViewIsShowing) wm.removeView(joinView);
        if (proViewIsShowing) wm.removeView(proView);
        if (voteViewIsShowing) wm.removeView(voteView);
    }

    private void setIsShowing(View remove, View add) {
        String removeTag = (String) remove.getTag();
        String addTag = (String) add.getTag();
        switch (removeTag) {
            case "hoverButton":
                hoverButtonIsShowing = false;
                break;
            case "menuView":
                menuViewIsShowing = false;
                break;
            case "serviceView":
                serviceViewIsShowing = false;
                break;
            case "screenView":
                screenViewIsShowing = false;
                break;
            case "joinView":
                joinViewIsShowing = false;
                break;
            case "proView":
                proViewIsShowing = false;
                break;
            case "voteView":
                voteViewIsShowing = false;
                break;
        }
        switch (addTag) {
            case "hoverButton":
                hoverButtonIsShowing = true;
                break;
            case "menuView":
                menuViewIsShowing = true;
                break;
            case "serviceView":
                serviceViewIsShowing = true;
                break;
            case "screenView":
                screenViewIsShowing = true;
                break;
            case "joinView":
                joinViewIsShowing = true;
                break;
            case "proView":
                proViewIsShowing = true;
                break;
            case "voteView":
                voteViewIsShowing = true;
                break;
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy -->");
        super.onDestroy();
        delAllView();
        presenter.unregister();
    }

//    public static class MenuViewHolder {
//        public View rootView;
//        public Button wm_menu_note;
//        public Button wm_menu_soft;
//        public Button wm_menu_hand;
//        public Button wm_menu_service;
//        public Button wm_menu_start_projection;
//        public Button wm_menu_back;
//        public Button wm_menu_stop_projection;
//        public Button wm_menu_start_screen;
//        public Button wm_menu_join_screen;
//        public Button wm_menu_stop_screen;
//        public Button wm_menu_screenshot;
//
//        public MenuViewHolder(View rootView) {
//            this.rootView = rootView;
//            this.wm_menu_note = (Button) rootView.findViewById(R.id.wm_menu_note);
//            this.wm_menu_soft = (Button) rootView.findViewById(R.id.wm_menu_soft);
//            this.wm_menu_hand = (Button) rootView.findViewById(R.id.wm_menu_hand);
//            this.wm_menu_service = (Button) rootView.findViewById(R.id.wm_menu_service);
//            this.wm_menu_start_projection = (Button) rootView.findViewById(R.id.wm_menu_start_projection);
//            this.wm_menu_back = (Button) rootView.findViewById(R.id.wm_menu_back);
//            this.wm_menu_stop_projection = (Button) rootView.findViewById(R.id.wm_menu_stop_projection);
//            this.wm_menu_start_screen = (Button) rootView.findViewById(R.id.wm_menu_start_screen);
//            this.wm_menu_join_screen = (Button) rootView.findViewById(R.id.wm_menu_join_screen);
//            this.wm_menu_stop_screen = (Button) rootView.findViewById(R.id.wm_menu_stop_screen);
//            this.wm_menu_screenshot = (Button) rootView.findViewById(R.id.wm_menu_screenshot);
//        }
//
//    }
//
//    public static class ServiceViewHolder {
//        public View rootView;
//        public TextView textView;
//        public ImageView wm_service_close;
//        public Button wm_service_pen;
//        public Button wm_service_pager;
//        public Button wm_service_tea;
//        public Button wm_service_calculate;
//        public Button wm_service_waiter;
//        public Button wm_service_clean;
//        public EditText wm_service_edt;
//        public Button wm_service_send;
//
//        public ServiceViewHolder(View rootView) {
//            this.rootView = rootView;
//            this.textView = (TextView) rootView.findViewById(R.id.textView);
//            this.wm_service_close = (ImageView) rootView.findViewById(R.id.wm_service_close);
//            this.wm_service_pen = (Button) rootView.findViewById(R.id.wm_service_pen);
//            this.wm_service_pager = (Button) rootView.findViewById(R.id.wm_service_pager);
//            this.wm_service_tea = (Button) rootView.findViewById(R.id.wm_service_tea);
//            this.wm_service_calculate = (Button) rootView.findViewById(R.id.wm_service_calculate);
//            this.wm_service_waiter = (Button) rootView.findViewById(R.id.wm_service_waiter);
//            this.wm_service_clean = (Button) rootView.findViewById(R.id.wm_service_clean);
//            this.wm_service_edt = (EditText) rootView.findViewById(R.id.wm_service_edt);
//            this.wm_service_send = (Button) rootView.findViewById(R.id.wm_service_send);
//        }
//
//    }
//
//    public static class ScreenViewHolder {
//        public View rootView;
//        public CheckBox wm_screen_mandatory;
//        public TextView wm_screen_title;
//        public TextView textView2;
//        public CheckBox wm_screen_cb_attendee;
//        public Button wm_screen_launch;
//        public Button wm_screen_cancel;
//        public CheckBox wm_screen_cb_projector;
//        public RecyclerView wm_screen_rv_attendee;
//        public RecyclerView wm_screen_rv_projector;
//
//        public ScreenViewHolder(View rootView) {
//            this.rootView = rootView;
//            this.wm_screen_mandatory = (CheckBox) rootView.findViewById(R.id.wm_screen_mandatory);
//            this.wm_screen_title = (TextView) rootView.findViewById(R.id.wm_screen_title);
//            this.textView2 = (TextView) rootView.findViewById(R.id.textView2);
//            this.wm_screen_cb_attendee = (CheckBox) rootView.findViewById(R.id.wm_screen_cb_attendee);
//            this.wm_screen_launch = (Button) rootView.findViewById(R.id.wm_screen_launch);
//            this.wm_screen_cancel = (Button) rootView.findViewById(R.id.wm_screen_cancel);
//            this.wm_screen_cb_projector = (CheckBox) rootView.findViewById(R.id.wm_screen_cb_projector);
//            this.wm_screen_rv_attendee = (RecyclerView) rootView.findViewById(R.id.wm_screen_rv_attendee);
//            this.wm_screen_rv_projector = (RecyclerView) rootView.findViewById(R.id.wm_screen_rv_projector);
//        }
//
//    }
//
//    public static class ProViewHolder {
//        public View rootView;
//        public CheckBox wm_pro_mandatory;
//        public TextView wm_pro_title;
//        public CheckBox wm_pro_all;
//        public RecyclerView wm_pro_rv;
//        public CheckBox wm_pro_full;
//        public CheckBox wm_pro_flow1;
//        public CheckBox wm_pro_flow2;
//        public CheckBox wm_pro_flow3;
//        public CheckBox wm_pro_flow4;
//        public Button wm_pro_launch_pro;
//        public Button wm_pro_cancel;
//
//        public ProViewHolder(View rootView) {
//            this.rootView = rootView;
//            this.wm_pro_mandatory = (CheckBox) rootView.findViewById(R.id.wm_pro_mandatory);
//            this.wm_pro_title = (TextView) rootView.findViewById(R.id.wm_pro_title);
//            this.wm_pro_all = (CheckBox) rootView.findViewById(R.id.wm_pro_all);
//            this.wm_pro_rv = (RecyclerView) rootView.findViewById(R.id.wm_pro_rv);
//            this.wm_pro_full = (CheckBox) rootView.findViewById(R.id.wm_pro_full);
//            this.wm_pro_flow1 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow1);
//            this.wm_pro_flow2 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow2);
//            this.wm_pro_flow3 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow3);
//            this.wm_pro_flow4 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow4);
//            this.wm_pro_launch_pro = (Button) rootView.findViewById(R.id.wm_pro_launch_pro);
//            this.wm_pro_cancel = (Button) rootView.findViewById(R.id.wm_pro_cancel);
//        }
//
//    }
//
//    public static class VoteViewHolder {
//        public View rootView;
//        public TextView wm_vote_title;
//        public TextView wm_vote_type;
//        public CheckBox checkBox1;
//        public CheckBox checkBox2;
//        public CheckBox checkBox3;
//        public CheckBox checkBox4;
//        public CheckBox checkBox5;
//        public Chronometer wm_vote_chronometer;
//        public Button wm_vote_submit;
//
//        public VoteViewHolder(View rootView) {
//            this.rootView = rootView;
//            this.wm_vote_title = (TextView) rootView.findViewById(R.id.wm_vote_title);
//            this.wm_vote_type = (TextView) rootView.findViewById(R.id.wm_vote_type);
//            this.checkBox1 = (CheckBox) rootView.findViewById(R.id.checkBox1);
//            this.checkBox2 = (CheckBox) rootView.findViewById(R.id.checkBox2);
//            this.checkBox3 = (CheckBox) rootView.findViewById(R.id.checkBox3);
//            this.checkBox4 = (CheckBox) rootView.findViewById(R.id.checkBox4);
//            this.checkBox5 = (CheckBox) rootView.findViewById(R.id.checkBox5);
//            this.wm_vote_chronometer = (Chronometer) rootView.findViewById(R.id.wm_vote_chronometer);
//            this.wm_vote_submit = (Button) rootView.findViewById(R.id.wm_vote_submit);
//        }
//
//    }
}
