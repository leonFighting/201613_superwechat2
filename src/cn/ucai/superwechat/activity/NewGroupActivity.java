/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.listener.OnSetAvatarListener;

import com.easemob.exceptions.EaseMobException;

public class NewGroupActivity extends BaseActivity {
	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox checkBox;
	private CheckBox memberCheckbox;
	private LinearLayout openInviteContainer;
	ImageView mivGroupAvatar;
	OnSetAvatarListener mOnSetAvatarListener;/**上传群头像*/
	String groupAvatarName;/**临时存储头像*/
	Activity mContext;
	//区别onActivityResult,requestCode,不是上传头像的执行创建群组
	private static final int CREATE_NEW_GROUP = 23;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_new_group);
		initView();
		setListener();
	}

	private void initView() {
		groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
		checkBox = (CheckBox) findViewById(R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
		openInviteContainer = (LinearLayout) findViewById(R.id.ll_open_invite);
		mivGroupAvatar = (ImageView) findViewById(R.id.iv_group_icon);
	}

	private void setListener() {
		setOnCheckChangedListener();
		setSaveGroupClickListener();
		setGroupIconClickListener();
	}

	private void setOnCheckChangedListener() {
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					openInviteContainer.setVisibility(View.INVISIBLE);
				}else{
					openInviteContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void setGroupIconClickListener() {
		findViewById(R.id.layout_group_icon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mOnSetAvatarListener = new OnSetAvatarListener(mContext, R.id.layout_new_group,
						getGroupAvatarName(), I.AVATAR_TYPE_GROUP_PATH);
			}
		});
	}

	private String getGroupAvatarName() {
		groupAvatarName = System.currentTimeMillis() + "";
		return groupAvatarName;
	}

	public void setSaveGroupClickListener() {
		findViewById(R.id.btn_save_group).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
				String name = groupNameEditText.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Intent intent = new Intent(mContext, AlertDialog.class);
					intent.putExtra("msg", str6);
					startActivity(intent);
				} else {
					// 进通讯录选人
					startActivityForResult(new Intent(mContext, GroupPickContactsActivity.class)
							.putExtra("groupName", name), CREATE_NEW_GROUP);
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == CREATE_NEW_GROUP) {
			setProgressDialog();
			//新建群组
			//1.创建环信群组 2.创建远端群组并且上传群组头像 3.添加群组成员到远端

			createNewGroup(data);
		} else {
			mOnSetAvatarListener.setAvatar(requestCode,data,mivGroupAvatar);
		}
	}

	private void setProgressDialog() {
		String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(st1);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

	}

	private void createNewGroup(final Intent data) {
		final String st2 = getResources().getString(R.string.Failed_to_create_groups);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 调用sdk创建群组方法
				String groupName = groupNameEditText.getText().toString().trim();
				String desc = introductionEditText.getText().toString();
				String[] members = null;
				//创建远端群组id
				String[] membersIds = null;
				Contact[] contacts = (Contact[]) data.getSerializableExtra("newmembers");
				if (contacts != null) {
					members = new String[contacts.length];
					membersIds = new String[contacts.length];
					for (int i = 0; i < contacts.length; i++) {
						members[i] = contacts[i].getMContactCname() + ",";
						membersIds[i] = contacts[i].getMContactCid() + ",";
					}
				}
				//获取环信id
				EMGroup emGroup;
				try {
					if(checkBox.isChecked()){
						//创建公开群，此种方式创建的群，可以自由加入
						//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
						emGroup=EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
					}else{
						//创建不公开群
						emGroup=EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(),200);
					}
					String hxid = emGroup.getGroupId();
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
						}
					});
				} catch (final EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}

			}
		}).start();
	}

	public void back(View view) {
		finish();
	}
}
