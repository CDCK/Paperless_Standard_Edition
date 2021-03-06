package xlk.paperless.standard.view.fragment.other.bulletin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceBullet;
import java.util.ArrayList;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.BulletAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author xlk
 * @date 2020/4/8
 * @desc 公告管理
 */
public class BulletinFragment extends BaseFragment implements IBulletin, View.OnClickListener {
    private RecyclerView f_bulletin_rv;
    private EditText f_bulletin_title;
    private EditText f_bulletin_content;
    private Button f_bulletin_add;
    private Button f_bulletin_del;
    private Button f_bulletin_modify;
    private Button f_bulletin_launch;
    private Button f_bulletin_close;
    private BulletinPresenter presenter;
    private BulletAdapter bulletAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_bulletin, container, false);
        initView(inflate);
        presenter = new BulletinPresenter(getContext(), this);
        presenter.queryNotice();
        return inflate;
    }

    private void initView(View inflate) {
        f_bulletin_rv = inflate.findViewById(R.id.f_bulletin_rv);
        f_bulletin_title = inflate.findViewById(R.id.f_bulletin_title);
        f_bulletin_content = inflate.findViewById(R.id.f_bulletin_content);
        f_bulletin_add = inflate.findViewById(R.id.f_bulletin_add);
        f_bulletin_del = inflate.findViewById(R.id.f_bulletin_del);
        f_bulletin_modify = inflate.findViewById(R.id.f_bulletin_modify);
        f_bulletin_launch = inflate.findViewById(R.id.f_bulletin_launch);
        f_bulletin_close = inflate.findViewById(R.id.f_bulletin_close);

        f_bulletin_add.setOnClickListener(this);
        f_bulletin_del.setOnClickListener(this);
        f_bulletin_modify.setOnClickListener(this);
        f_bulletin_launch.setOnClickListener(this);
        f_bulletin_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_bulletin_add:
                String title = f_bulletin_title.getText().toString();
                String content = f_bulletin_content.getText().toString();
                if (title.isEmpty() || content.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_info);
                } else if (title.length() > Constant.MAX_TITLE_LENGTH) {
                    ToastUtil.show(getString(R.string.err_title_max_length, Constant.MAX_TITLE_LENGTH + ""));
                } else if (content.length() > Constant.MAX_CONTENT_LENGTH) {
                    ToastUtil.show(getString(R.string.err_bulletin_max_length, Constant.MAX_CONTENT_LENGTH + ""));
                } else {
                    InterfaceBullet.pbui_Item_BulletDetailInfo build = InterfaceBullet.pbui_Item_BulletDetailInfo.newBuilder()
                            .setTitle(s2b(title))
                            .setContent(s2b(content)).build();
                    JniHandler.getInstance().addNotice(build);
                }
                break;
            case R.id.f_bulletin_del:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    JniHandler.getInstance().deleteNotice(bulletAdapter.getChoose());
                } else {
                    ToastUtil.show(R.string.please_choose_bulletin);
                }
                break;
            case R.id.f_bulletin_modify:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    String title1 = f_bulletin_title.getText().toString();
                    String content1 = f_bulletin_content.getText().toString();
                    if (title1.isEmpty() || content1.isEmpty()) {
                        ToastUtil.show(R.string.please_enter_info);
                    } else if (title1.length() > Constant.MAX_TITLE_LENGTH) {
                        ToastUtil.show(getString(R.string.err_title_max_length, Constant.MAX_TITLE_LENGTH + ""));
                    } else if (content1.length() > Constant.MAX_CONTENT_LENGTH) {
                        ToastUtil.show(getString(R.string.err_bulletin_max_length, Constant.MAX_CONTENT_LENGTH + ""));
                    } else {
                        InterfaceBullet.pbui_Item_BulletDetailInfo build = InterfaceBullet.pbui_Item_BulletDetailInfo.newBuilder()
                                .setTitle(s2b(title1))
                                .setContent(s2b(content1)).build();
                        JniHandler.getInstance().modifNotice(build);
                    }
                } else {
                    ToastUtil.show(R.string.please_choose_bulletin);
                }
                break;
            case R.id.f_bulletin_launch:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    JniHandler.getInstance().pushNotice(bulletAdapter.getChoose(), new ArrayList<>());
                } else {
                    ToastUtil.show(R.string.please_choose_bulletin);
                }
                break;
            case R.id.f_bulletin_close:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    JniHandler.getInstance().stopNotice(bulletAdapter.getChoose().getBulletid(), new ArrayList<>());
                } else {
                    ToastUtil.show(R.string.please_choose_bulletin);
                }
                break;
        }
    }

    @Override
    public void notifyAdapter() {
        if (bulletAdapter == null) {
            bulletAdapter = new BulletAdapter(R.layout.item_bullet, presenter.bulletInfos);
            f_bulletin_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_bulletin_rv.setAdapter(bulletAdapter);
        } else {
            bulletAdapter.notifyDataSetChanged();
        }
        bulletAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                InterfaceBullet.pbui_Item_BulletDetailInfo info = presenter.bulletInfos.get(position);
                bulletAdapter.choose(info.getBulletid());
                updateUI(info);
            }
        });
        InterfaceBullet.pbui_Item_BulletDetailInfo choose = bulletAdapter.getChoose();
        if (choose == null) {
            if (!presenter.bulletInfos.isEmpty()) {
                bulletAdapter.choose(presenter.bulletInfos.get(0).getBulletid());
                updateUI(presenter.bulletInfos.get(0));
            } else {
                updateUI(null);
            }
        }
    }

    private void updateUI(InterfaceBullet.pbui_Item_BulletDetailInfo info) {
        f_bulletin_title.setText("");
        f_bulletin_content.setText("");
        if (info != null) {
            f_bulletin_title.setText(info.getTitle().toStringUtf8());
            f_bulletin_content.setText(info.getContent().toStringUtf8());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
