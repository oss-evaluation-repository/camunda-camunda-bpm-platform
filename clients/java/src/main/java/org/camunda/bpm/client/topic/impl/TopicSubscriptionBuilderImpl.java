/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.topic.impl;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionBuilderImpl implements TopicSubscriptionBuilder {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String topicName;
  protected long lockDuration;
  protected ExternalTaskHandler externalTaskHandler;
  protected TopicSubscriptionManager topicSubscriptionManager;

  public TopicSubscriptionBuilderImpl(String topicName, TopicSubscriptionManager topicSubscriptionManager) {
    this.topicName = topicName;
    this.topicSubscriptionManager = topicSubscriptionManager;
  }

  public TopicSubscriptionBuilder lockDuration(long lockDuration) {
    this.lockDuration = lockDuration;
    return this;
  }

  public TopicSubscriptionBuilder handler(ExternalTaskHandler externalTaskHandler) {
    this.externalTaskHandler = externalTaskHandler;
    return this;
  }

  public TopicSubscription open() {
    if (topicName == null) {
      throw LOG.topicNameNullException();
    }

    if (lockDuration <= 0L) {
      throw LOG.lockDurationIsNotGreaterThanZeroException();
    }

    if (externalTaskHandler == null) {
      throw LOG.lockedTaskHandlerNullException();
    }

    // TODO: synchronize check + add (+remove)
    checkTopicNameAlreadySubscribed();

    TopicSubscriptionImpl subscription = new TopicSubscriptionImpl(topicName, lockDuration, externalTaskHandler, topicSubscriptionManager);
    topicSubscriptionManager.subscribe(subscription);

    return subscription;
  }

  protected void checkTopicNameAlreadySubscribed() {
    List<TopicSubscriptionImpl> subscriptions = topicSubscriptionManager.getSubscriptions();
    subscriptions.forEach(subscription -> {
      if (subscription.getTopicName().equals(topicName)) {
        throw LOG.topicNameAlreadySubscribedException();
      }
    });
  }

}
