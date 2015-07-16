package thebombzen.mods.autoswitch.configuration;

public class EntityWeaponPair {
		
		private EntityIdentifier entity;
		private BlockItemIdentifier weapon;

		public EntityWeaponPair(EntityIdentifier entity, BlockItemIdentifier weapon) {
			this.entity = entity;
			this.weapon = weapon;
		}

		public EntityIdentifier getEntity() {
			return entity;
		}

		public BlockItemIdentifier getWeapon() {
			return weapon;
		}

		public void setBlock(EntityIdentifier entity) {
			this.entity = entity;
		}

		public void setTool(BlockItemIdentifier weapon) {
			this.weapon = weapon;
		}

		@Override
		public String toString() {
			return entity + ", " + weapon;
		}


}
