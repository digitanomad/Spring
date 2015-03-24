package springbook.user.service;

import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserLevelUpgradePolicyUsual implements UserLevelUpgradePolicy {

	public boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();

		switch (currentLevel) {
		case BASIC:
			return (user.getLogin() >= UserService.MIN_LOGCOUNT_FOR_SILVER);
		case SILVER:
			return (user.getRecommend() >= UserService.MIN_RECOMMEND_FOR_GOLD);
		case GOLD:
			return false;
		default:
			throw new IllegalArgumentException("Unknown Level: " + currentLevel);
		}
	}

	public void upgradeLevel(User user) {
		user.upgradeLevel();
	}

}
