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

package org.projectmaxs.module.wifiaccess.commands;

import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;

public class WifiList extends AbstractWifi {

	public WifiList() {
		super("list");
		setHelp(ArgType.NONE, "List all configured Wifi networks");
	}

	// Newer Android APIs require the ACCESS_FINE_LOCATION permission for WifiManager.getConfiguredNetworks.
	@SuppressLint("MissingPermission")
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		super.execute(arguments, command, service);

		Message msg = new Message();
		List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration w : networks) {
			Text text = new Text();
			text.addBold("ID: ").add(Integer.toString(w.networkId)).addBold(" Name: ")
					.addNL(w.SSID);
			text.addBold("Status: ").addNL(statusIntToString(w.status));
			msg.add(text);
		}

		return msg;
	}

	private static final String statusIntToString(int statusInt) {
		String status;
		switch (statusInt) {
		case WifiConfiguration.Status.CURRENT:
			status = "Current connected network";
			break;
		case WifiConfiguration.Status.ENABLED:
			status = "Enabled";
			break;
		case WifiConfiguration.Status.DISABLED:
			status = "Disabled";
			break;
		default:
			status = "Unknown";
			break;
		}
		return status;
	}
}
