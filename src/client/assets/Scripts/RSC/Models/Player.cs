namespace Assets.RSC.Models
{
	public class Player : Mob
	{
		public string Username { get; set; }

		public int CombatLevel
		{
			get
			{
				if (Level > 0)
					return Level;

				return (StatBase[0] + StatBase[1] + StatBase[2] + StatBase[3]) / 4;
			}
		}

		public int[] StatCurrent = new int[Mudclient.SkillName.Length];

		public int[] StatBase = new int[Mudclient.SkillName.Length];

		public long[] StatExp = new long[Mudclient.SkillName.Length];


		public Player()
		{

		}
	}
}
