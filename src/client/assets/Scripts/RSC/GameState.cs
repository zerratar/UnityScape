using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets.RSC
{
	using System.Collections;
	using System.ComponentModel;

	using Assets.RSC.IO;
	using Assets.RSC.Managers;
	using Assets.RSC.Models;
	using Assets.RSC.Network;

	using UnityEngine;
	using UnityEngine.UI;

	using Camera = UnityEngine.Camera;
	using GameObject = UnityEngine.GameObject;

	public class GameState : MonoBehaviour
	{
		public string Server_IP = "192.99.152.220";
		public int Server_Port = 53595;
		public Mudclient Client { get; set; }


		private GameObject loadingPanel;
		private GameObject loginPanel;
		private GameObject gamePanel;

		public GameObject Terrain, Wall, Roof, Floor;

		//	public Terrain terrain;

		private Text loginResult;
		private Text loadText;
		private Text debugText;

		private bool loginFailed = false;
		private bool loginSuccess = false;
		private bool newLoginMessage = false;
		private bool gameActive = false;

		public bool gameDataLoaded = false;
		private bool gameDataLoadProgress = false;
		private bool gameDataLoadStarted = false;
		private bool gameDataLoadVisible = true;
		private int gameDataLoadProgressMin = 0;
		private int gameDataLoadProgressMax = 0;
		private BackgroundWorker gameDataLoader;

		private string username;
		private string password;

		public bool DrawLogo = true;

	    public int activeSectionX = 51, activeSectionY = 50;

        public void Start()
		{
			loadingPanel = GameObject.Find("LoadingPanel");
			loginResult = gameObject.Find<Text>("LoginResult");
			loginPanel = GameObject.Find("LoginPanel");
			loadText = gameObject.Find<Text>("LoadText");
			debugText = gameObject.Find<Text>("DebugText");

			// if (!terrain)
			//	terrain = gameObject.Find<Terrain>("Terrain");

			gamePanel = GameObject.Find("GamePanel");



			gamePanel.SetActive(false);

			//Floor = Terrain.transform.FindChild("GameFloor").gameObject;
			//Wall = Terrain.transform.FindChild("GameWall").gameObject;
			//Roof = Terrain.transform.FindChild("GameRoof").gameObject;



			loginPanel.SetActive(false);
		}


		public void UpdateTerrain()
		{
			this.Terrain.GetComponent<MeshFilter>().sharedMesh = Engine.Terrain.Mesh;
			this.Terrain.GetComponent<MeshCollider>().sharedMesh = Engine.Terrain.Mesh;


			
			this.Wall.GetComponent<MeshFilter>().sharedMesh = Engine.Wall.Mesh;
			this.Wall.GetComponent<MeshCollider>().sharedMesh = Engine.Wall.Mesh;
			
			if (TextureManager.TextureAtlas != null)
			{
				var wallRenderer = this.Wall.GetComponent<MeshRenderer>();
				var wallMat = wallRenderer.material;
				wallMat.mainTexture = TextureManager.TextureAtlas;
			}


			this.Roof.GetComponent<MeshFilter>().sharedMesh = Engine.Roof.Mesh;
			this.Roof.GetComponent<MeshCollider>().sharedMesh = Engine.Roof.Mesh;

			this.Floor.GetComponent<MeshFilter>().sharedMesh = Engine.Floor.Mesh;
			this.Floor.GetComponent<MeshCollider>().sharedMesh = Engine.Floor.Mesh;

		}

		public void Update()
		{


			if (gameDataLoaded && Mudclient.TerrainUpdateNecessary)
			{
				Mudclient.TerrainUpdateNecessary = false;

				Engine.Terrain.GenerateMesh();
				Engine.Wall.GenerateMesh();
				Engine.Roof.GenerateMesh();
				Engine.Floor.GenerateMesh();

				UpdateTerrain();

				// activeSectionX = Mudclient.SectionX;
				// activeSectionY = Mudclient.SectionY;
			}


			if (gameDataLoaded)
			{
				if (gameActive && Mudclient.TerrainUpdateNecessary)
				{
					Mudclient.TerrainUpdateNecessary = false;
					// activeSectionX = Mudclient.SectionX;
					// activeSectionY = Mudclient.SectionY;
					// Engine.loadSection(Mudclient.SectionX, Mudclient.SectionY);

					// UpdateTerrain();
				}
				if (Input.GetKeyUp(KeyCode.LeftArrow))
				{
					activeSectionX++;


					Engine.loadSection(activeSectionX, activeSectionY);

				}
				if (Input.GetKeyUp(KeyCode.RightArrow))
				{

					activeSectionX--;
					if (activeSectionX < 0) activeSectionX = 0;
					Engine.loadSection(activeSectionX, activeSectionY);

				}

				if (Input.GetKeyUp(KeyCode.UpArrow))
				{
					activeSectionY++;
					Engine.loadSection(activeSectionX, activeSectionY);

				}
				if (Input.GetKeyUp(KeyCode.DownArrow))
				{

					activeSectionY--;
					if (activeSectionY < 0) activeSectionY = 0;
					Engine.loadSection(activeSectionX, activeSectionY);

				}
			}
			if (!gameDataLoaded)
			{
				if (!gameDataLoadStarted)
				{
					gameDataLoadStarted = true;
					StartCoroutine(LoadGameData());
				}

				if (gameDataLoadProgress)
				{
					gameDataLoadProgress = false;
				}
			}
			if (gameDataLoaded)
			{
				if (gameDataLoadVisible)
				{
					loadingPanel.SetActive(false);
					gameDataLoadVisible = false;
				}

				if (Client != null)
				{
					if (gameActive)
					{
						UpdateGameView();
						return;
					}
					UpdateLoginView();
				}
			}
		}

		public IEnumerator LoadGameData()
		{
			// gameDataLoader = new BackgroundWorker();
			// gameDataLoader.DoWork += (e, v) =>
			yield return new WaitForEndOfFrame();
			{
				DataLoader.LoadAll(
					(c, m) =>
					{
						gameDataLoadProgressMin = c;
						gameDataLoadProgressMax = m;
						gameDataLoadProgress = true;
						var proc = (int)(((float)c / (float)m) * 100f);
						// 	Debug.Log(proc + "% loaded");
						loadText.text = "Loading game data... " + proc + "%";
						Camera.main.Render();
						//	SceneView.RepaintAll();
					});


				Engine.loadSection(activeSectionX, activeSectionY/*50*/);

				//	Engine.Terrain.GenerateMesh();

				// UpdateTerrain();


				//	terrain.terrainData.SetHeights(0, 0, hm);
				//	terrain.Flush();
				//	terrain.terrainData.
				gameDataLoaded = true;
			};
			// gameDataLoader.RunWorkerAsync();
		}

		public void OnGUI()
		{
			if (!gameActive && gameDataLoaded)
			{
				if (DataLoader.LoadedIcons.ContainsKey(DataLoader.baseInventoryPic + 10) && DrawLogo)
				{

					var currentSprite = DataLoader.LoadedIcons[DataLoader.baseInventoryPic + 10];// spriteArray[activeItemIndex];
					if (currentSprite != null)
					{
						GUI.DrawTexture(
							new Rect((Screen.width / 2) + (currentSprite.width / 2), 10,
								-currentSprite.width, currentSprite.height),
							currentSprite
						);

					}
				}
			}
			if (gameActive)
			{
				if (Inventory.Instance.Items != null && Inventory.Instance.Items.Length > 0)
				{
					sbyte byte0 = 22;
					sbyte byte1 = 36;

					for (int k5 = 0; k5 < Inventory.Instance.Items.Length; k5++)
					{
						int l5 = 217 + byte0 + (k5 % 5) * 49;
						int j6 = 31 + byte1 + (k5 / 5) * 34;

						if (Inventory.Instance.Items[k5] != null && Inventory.Instance.Items[k5].Image != null)
							GUI.DrawTexture(
									new Rect(l5, j6,
										Inventory.Instance.Items[k5].Image.width, Inventory.Instance.Items[k5].Image.height),
									Inventory.Instance.Items[k5].Image
								);
						/*
						gameGraphics.drawImage(l5, j6, 48, 32, baseItemPicture + Data.itemInventoryPicture[inventoryItems[k5]], Data.itemPictureMask[inventoryItems[k5]], 0, 0, false);
						if (Data.Data.itemStackable[inventoryItems[k5]] == 0)
							gameGraphics.drawString(inventoryItemCount[k5].ToString(), l5 + 1, j6 + 10, 1, 0xffff00); */
					}
				}
			}
		}

		public void UpdateGameView()
		{



			debugText.text = "Username: " + MobManager.MyPlayer.Username + " - " + MobManager.MyPlayer.CombatLevel + " pos: " + MobManager.MyPlayer.currentX + ", " + MobManager.MyPlayer.currentY + Environment.NewLine;

			int i = 0;
			while (true)
			{
				if (i >= Mudclient.SkillName.Length) break;
				debugText.text += Mudclient.SkillName[i] + " " + MobManager.MyPlayer.StatCurrent[i] + " / " + MobManager.MyPlayer.StatBase[i] + "          ";
				i++;

				if (i >= Mudclient.SkillName.Length) break;
				debugText.text += Mudclient.SkillName[i] + " " + MobManager.MyPlayer.StatCurrent[i] + " / " + MobManager.MyPlayer.StatBase[i] + Environment.NewLine;
				i++;
			}



		}

		public void UpdateLoginView()
		{
			if (newLoginMessage)
			{
				loginResult.text = Client.LoginMessageRow1 + " " + Client.LoginMessageRow2;
				newLoginMessage = false;
			}

			if (loginSuccess)
			{
				gamePanel.SetActive(true);
				loginPanel.SetActive(false);
				gameActive = true;
				loginSuccess = false;
			}

			if (loginFailed)
			{
				loginFailed = false;
			}
		}


		public void LoginClick()
		{
			var user = gameObject.Find<InputField>("InputUsername");
			var pass = gameObject.Find<InputField>("InputPassword");

			if (Client == null)
			{
				Client = new Mudclient(Server_IP, Server_Port);
				Client.OnDisplayLoginMessage += Client_OnDisplayLoginMessage;
				Client.OnConnectSuccess += Client_OnConnectSuccess;
				Client.OnConnectFailed += Client_OnConnectFailed;
			}
			username = user.text;
			password = pass.text;
			Client.connect(username, password, false);
		}

		void Client_OnConnectFailed(object sender, EventArgs e)
		{
			loginFailed = true;
		}

		void Client_OnConnectSuccess(object sender, EventArgs e)
		{
			loginSuccess = true;

			InitPlayerVars();

		}

		private void InitPlayerVars()
		{
			if (MobManager.MyPlayer == null)
				MobManager.MyPlayer = new Player();

			MobManager.MyPlayer.Username = username;
		}

		void Client_OnDisplayLoginMessage(object sender, EventArgs e)
		{
			newLoginMessage = true;
			Debug.Log(Client.LoginMessageRow1 + " " + Client.LoginMessageRow2);

		}


		private void OnDestroy()
		{
			if (Client != null)
			{

				Client.requestLogout();



				Client.Disconnect();
			}
		}

	}
}
