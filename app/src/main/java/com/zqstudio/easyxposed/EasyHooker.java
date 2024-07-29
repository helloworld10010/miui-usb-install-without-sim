package com.zqstudio.easyxposed;

import com.zqstudio.easyxposed.utils.Tool;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.zqstudio.easyxposed.utils.Hool.hookMethod;
import static com.zqstudio.easyxposed.utils.Tool.myLog;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import java.lang.reflect.Method;


public final class EasyHooker implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
		// WARN：去LSP中确认，实际要被hook的应用包名
		String strApp = "com.miui.securitycenter";
		if (!strApp.equals(lpparam.packageName)){
			return;
		}

		XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context context = (Context) param.args[0];
				lpparam.classLoader = context.getClassLoader();
				Tool.classLoader = context.getClassLoader();
				myLog("classLoader ................."+Tool.classLoader);

				appHook();
			}
		});

		myLog("handleLoadPackage Hook Start ...................................");

	}

	private void appHook() throws ClassNotFoundException {
		Class<?> clazz = Tool.classLoader.loadClass("com.miui.permcenter.install.AdbInstallVerifyActivity$a");

		XposedHelpers.findAndHookMethod(clazz, "onPostExecute", Object.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.args[0] = null;
				myLog("before onPostExecute ...");
			}
		});


		hookMethod("com.miui.permcenter.install.AdbInstallVerifyActivity",
				"C", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						super.beforeHookedMethod(param);
						XposedHelpers.setBooleanField(param.thisObject,"d",false);
						myLog("C ...");
					}
				});


//		/*自动直接安装*/

		hookMethod("com.miui.permcenter.install.AdbInstallActivity", "C",
				new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) {
				myLog("count down C ................................ before");
				XposedHelpers.callMethod(param.thisObject,"onClick",
						new Class[]{DialogInterface.class,int.class},null,-2);
				XposedHelpers.callMethod(param.thisObject,"finish");
			}
		});

	}
}
