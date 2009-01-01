/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.tools.deploy;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <a href="DeploymentProgressListener.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 * @author Sandeep Soni
 *
 */

public class DeploymentProgressListener implements ProgressListener {

	DeploymentProgressListener(
		JSR88DeploymentHandler driver, String warContext) {
		_handler = driver;
		_warContext = warContext;
		_deployManager = _handler.getDeploymentManager();
	}

	public void handleProgressEvent(ProgressEvent event) {
		_log.info(event.getDeploymentStatus().getMessage());

		if (event.getDeploymentStatus().isCompleted()) {
			try {
				TargetModuleID[] ids = _deployManager.getNonRunningModules(
					ModuleType.WAR, _deployManager.getTargets());

				TargetModuleID[] myIDs = new TargetModuleID[1];

				for (TargetModuleID id : ids) {
					if (_warContext.equals(id.getModuleID())) {
						myIDs[0] = id;
						ProgressObject startProgress =
							_deployManager.start(myIDs);

						startProgress.addProgressListener(
							new ProgressListener() {

									public void handleProgressEvent(
											ProgressEvent event) {

										_log.info(event
												.getDeploymentStatus()
												.getMessage());

										if (event.getDeploymentStatus()
												.isCompleted()) {
											_handler.setError(false);
											_handler.setAppStarted(true);
										}
									}
								});
					}
				}

				_handler.setError(false);
				_handler.setAppStarted(true);
			}
			catch (IllegalStateException ise) {
				_log.warn(ise);
				_handler.setError(true);
				_handler.setAppStarted(false);
			}
			catch (TargetException te) {
				_log.warn(te);
				_handler.setError(true);
				_handler.setAppStarted(false);
			}
		}
		else if (event.getDeploymentStatus().isFailed()) {
			_handler.setError(true);
			_handler.setAppStarted(false);
		}
	}

	private DeploymentManager _deployManager;
	private JSR88DeploymentHandler _handler;
	private String _warContext;

	private static Log _log = LogFactory.getLog(
		DeploymentProgressListener.class);
}