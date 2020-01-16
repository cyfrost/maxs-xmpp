/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.transport.xmpp.xmppservice;

import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.tcp.BundleAndDefer;
import org.jivesoftware.smack.tcp.BundleAndDeferCallback;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.projectmaxs.shared.global.util.Log;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

/**
 * Bundle And Defer means that Smack will invoke the
 * {@link BundleAndDeferCallback#getBundleAndDeferMillis(BundleAndDefer)} callback once it is about
 * to send a stanza that could be deferred. If it is deferred, all following stanzas will get
 * bundled. The return value of the callback is the time in milliseconds the stanza, and all
 * following, will get deferred.
 * <p>
 * Together with he callback, Smack hands out an reference to a {@link BundleAndDefer} instance,
 * which allows us to abort the current deferring and send all bundled stanzas right away. We do
 * this once Android reports that the network become active.
 * </p>
 *
 */
public class XMPPBundleAndDefer {

	/**
	 * How long Smack defers outgoing stanzas if the current network is in high power (active)
	 * state.
	 */
	private static final int ACTIVE_STATE_DEFER_MILLIS = 150;

	/**
	 * How long Smack defers outgoing stanzas if the current network is not in high power (inactive)
	 * state.
	 */
	private static final int INACTIVE_STATE_DEFER_MILLIS = 23 * 1000;

	private static final Log LOG = Log.getLog();

	/**
	 * Integer value indication when not to BAD (BundleAndDefer). If its value is greater zero, then
	 * bundle and
	 * defer while not take place.
	 */
	private static final AtomicInteger sDoNotBadInt = new AtomicInteger();

	/**
	 * The current BundleAndDefer instance, which can be used to stop the current bundle and defer
	 * process by Smack. Once it's stopped, the bundled stanzas so far will be send immediately.
	 */
	private static BundleAndDefer currentBundleAndDefer;

	@TargetApi(21)
	public static void initialize(final Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		BundleAndDeferCallback bundleAndDeferCallback = new BundleAndDeferCallback() {
			@Override
			public int getBundleAndDeferMillis(BundleAndDefer bundleAndDefer) {
				if (sDoNotBadInt.get() > 0) {
					return 0;
				}
				XMPPBundleAndDefer.currentBundleAndDefer = bundleAndDefer;
				String networkState = "unknown (needs Android >= 5.0)";
				boolean networkActive = false;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					if (connectivityManager.isDefaultNetworkActive()) {
						networkState = "active";
						networkActive = true;
					} else {
						networkState = "incative";
					}
				}
				boolean isPlugged = isPlugged(context);
				final int deferMillis;
				if (isPlugged || networkActive) {
					deferMillis = ACTIVE_STATE_DEFER_MILLIS;
				} else {
					deferMillis = INACTIVE_STATE_DEFER_MILLIS;
				}
				if (LOG.isDebugLogEnabled()) {
					LOG.d("Returning " + deferMillis
							+ "ms in getBundleAndDeferMillis(). Network is "
							+ networkState + ", batteryPlugged: " + isPlugged);
				}
				return deferMillis;
			}
		};
		XMPPTCPConnection.setDefaultBundleAndDeferCallback(bundleAndDeferCallback);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			connectivityManager.addDefaultNetworkActiveListener(new OnNetworkActiveListener() {
				@Override
				public void onNetworkActive() {
					stopCurrentBundleAndDefer();
				}
			});
		}
	}

	public static void stopCurrentBundleAndDefer() {
		final BundleAndDefer localCurrentbundleAndDefer = currentBundleAndDefer;
		if (localCurrentbundleAndDefer == null) {
			return;
		}
		LOG.d("stopCurrentBundleAndDefer() invoked and currentbundleAndDefer not null, calling stopCurrentBundleAndDefer()");
		localCurrentbundleAndDefer.stopCurrentBundleAndDefer();
	}

	/**
	 * Disables bundle and defer until {@link #enableBundleAndDefer()} is called.
	 */
	public static void disableBundleAndDefer() {
		sDoNotBadInt.incrementAndGet();
		stopCurrentBundleAndDefer();
	}

	/**
	 * Re-enables bundle and defer. {@link #disableBundleAndDefer()} must be called prior calling
	 * this.
	 */
	public static void enableBundleAndDefer() {
		sDoNotBadInt.decrementAndGet();
	}

	private static final IntentFilter BATTERY_CHANGED_INTENT_FILTER = new IntentFilter(
			Intent.ACTION_BATTERY_CHANGED);

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private static boolean isPlugged(Context context) {
		// BATTERY_CHANGED_INTENT is a sticky broadcast intent
		final Intent intent = context.registerReceiver(null, BATTERY_CHANGED_INTENT_FILTER);
		final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean isPlugged;
		switch (plugged) {
		case BatteryManager.BATTERY_PLUGGED_AC:
		case BatteryManager.BATTERY_PLUGGED_USB:
			isPlugged = true;
			break;
		default:
			isPlugged = false;
			break;
		}
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
			isPlugged |= plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
		}

		return isPlugged;
	}
}
