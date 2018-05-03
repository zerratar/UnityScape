using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets.RSC.IO
{
	using System.IO;

	using Assets.RSC.Managers;
	using Assets.RSC.Models;

	using UnityEngine;

	using GameObject = Assets.RSC.Models.GameObject;

	public class DataLoader
	{

		public static Dictionary<int, Texture2D> LoadedIcons = new Dictionary<int, Texture2D>();

		public static Dictionary<int, Texture2D> LoadedSprites = new Dictionary<int, Texture2D>();


		public static GameImage GameGraphics;

		public static int baseInventoryPic, baseScrollPic, baseItemPicture, baseLoginScreenBackgroundPic, baseProjectilePic, baseTexturePic, subTexturePic;


		public static void LoadAll(Action<int, int> progress)
		{
			// Simple progress update.. for now.
			if (progress != null) progress(0, 6);

			LoadConfig();
			if (progress != null) progress(1, 6);

			LoadMedia();
			if (progress != null) progress(2, 6);

			LoadAnimations();
			if (progress != null) progress(3, 6);

			LoadTextures();
			if (progress != null) progress(4, 6);

			LoadModels();
			if (progress != null) progress(5, 6);

			LoadMap();
			if (progress != null) progress(6, 6);
		}



		public static bool LoadConfig()
		{
			if (GameGraphics == null) GameGraphics = new GameImage(512, 346, 4000);

			sbyte[] abyte0 = UnpackData("config.jag", "Configuration", 10);
			if (abyte0 == null)
			{
				// errorLoading = true;
				return false;
			}

			Data.load(abyte0);
			sbyte[] abyte1 = UnpackData("filter.jag", "Chat system", 15);
			if (abyte1 == null)
			{
				// errorLoading = true;
				return false;
			}

			sbyte[] abyte2 = DataOperations.loadData("fragmentsenc.txt", 0, abyte1);
			sbyte[] abyte3 = DataOperations.loadData("badenc.txt", 0, abyte1);
			sbyte[] abyte4 = DataOperations.loadData("hostenc.txt", 0, abyte1);
			sbyte[] abyte5 = DataOperations.loadData("tldlist.txt", 0, abyte1);
			//ChatFilter.addFilterData(new DataEncryption(abyte2), new DataEncryption(abyte3), new DataEncryption(abyte4), new DataEncryption(abyte5));
			return true;
		}

		public static bool LoadModels()
		{
			if (GameGraphics == null) GameGraphics = new GameImage(512, 346, 4000);
			Data.getModelNameIndex("torcha2");
			Data.getModelNameIndex("torcha3");
			Data.getModelNameIndex("torcha4");
			Data.getModelNameIndex("skulltorcha2");
			Data.getModelNameIndex("skulltorcha3");
			Data.getModelNameIndex("skulltorcha4");
			Data.getModelNameIndex("firea2");
			Data.getModelNameIndex("firea3");
			Data.getModelNameIndex("fireplacea2");
			Data.getModelNameIndex("fireplacea3");
			Data.getModelNameIndex("firespell2");
			Data.getModelNameIndex("firespell3");
			Data.getModelNameIndex("lightning2");
			Data.getModelNameIndex("lightning3");
			Data.getModelNameIndex("clawspell2");
			Data.getModelNameIndex("clawspell3");
			Data.getModelNameIndex("clawspell4");
			Data.getModelNameIndex("clawspell5");
			Data.getModelNameIndex("spellcharge2");
			Data.getModelNameIndex("spellcharge3");
			sbyte[] modelData = UnpackData("models.jag", "3d models", 60);
			if (modelData == null)
			{
				return false;
			}
			for (int i1 = 0; i1 < Data.modelCount; i1++)
			{
				try
				{
					long objectOffset = DataOperations.getObjectOffset(Data.modelName[i1] + ".ob3", modelData);
					if (objectOffset != 0)
						GameObject.LoadedObjects.Add(new GameObject(modelData, (int)objectOffset, true));
					else
						GameObject.LoadedObjects.Add(new GameObject(1, 1));
					if (Data.modelName[i1].Equals("giantcrystal"))
						GameObject.LoadedObjects[i1].IsGiantCrystal = true;
				}
				catch { }
			}
			return true;
		}

		public static bool LoadTextures()
		{
			if (GameGraphics == null) GameGraphics = new GameImage(512, 346, 4000);
			sbyte[] _textureData = UnpackData("textures.jag", "Textures", 50);
			if (_textureData == null)
			{

				return false;
			}

			var baseInventoryPic = 2000;
			var baseScrollPic = baseInventoryPic + 100;
			var baseItemPicture = baseScrollPic + 50;
			var baseLoginScreenBackgroundPic = baseItemPicture + 1000;
			var baseProjectilePic = baseLoginScreenBackgroundPic + 10;
			var baseTexturePic = baseProjectilePic + 50;
			var subTexturePic = baseTexturePic + 10;

			sbyte[] indexData = DataOperations.loadData("index.dat", 0, _textureData);
			//gameCamera.CreateTexture(Data.textureCount, 7, 11);
			for (int l = 0; l < Data.textureCount; l++)
			{
				String s1 = Data.textureName[l];
				sbyte[] textureData = DataOperations.loadData(s1 + ".dat", 0, _textureData);



				var texture = TextureManager.CreateTexture(baseTexturePic + l, textureData, indexData, 1);



				if (l == 8 || l == 16 || l == 33 || l == 34)
				{


					// Rotate clockwise..
					if (texture != null && texture.Length == 1)
					{
						//	var t = texture[0];

						//	TextureBaker baker = new TextureBaker(Game1.DeviceInstance, new Vector2(t.Height, t.Width));
						//	baker.BakeTexture(t, RotateFlipType.Rotate90FlipNone);
						//	texture[0] = baker.GetTexture();

						texture[0] = TextureManager.Rotate(texture[0], RotateFlipType.Rotate270FlipNone);

						texture[0] = TextureManager.Rotate(texture[0], RotateFlipType.Rotate180FlipNone);
					}

				}


				String s2 = Data.textureSubName[l];
				if (string.IsNullOrEmpty(s2))
				{
					if (texture != null && texture.Length > 0)
					{
						for (var j = 0; j < texture.Length; j++)
						{

							//var bakerman = new TextureBaker(Game1.DeviceInstance, new Vector2(texture[j].Height, texture[j].Width));
							//bakerman.BakeTexture(texture[j], RotateFlipType.Rotate270FlipNone);
							//texture[j] = bakerman.GetTexture();
							/*
							using (var stream = System.IO.File.Create("c:/jpg/" + (subTexturePic + l) + ".png"))
							{
								texture[j].SaveAsPng(stream, texture[j].Width, texture[j].Height);
							}
							 */

							texture[j] = TextureManager.Rotate(texture[j], RotateFlipType.Rotate270FlipNone);

							texture[j] = TextureManager.Rotate(texture[j], RotateFlipType.Rotate180FlipNone);

							TextureManager.Textures.Add(new TextureData((subTexturePic + l/*textureIndex*/), texture[j]));

						}
					}
				}




				if (!string.IsNullOrEmpty(s2))
				{
					Texture2D backTexture = null;
					if (texture != null && texture.Any())
					{
						backTexture = texture[0];
					}
					sbyte[] subTextureData = DataOperations.loadData(s2 + ".dat", 0, _textureData);
					var subTexture = TextureManager.CreateTexture(baseTexturePic, subTextureData, indexData, 1, backTexture /*null*/);
					if (subTexture != null && subTexture.Length > 0)
					{
						foreach (var t in subTexture)
						{
							/*
							using (var stream = System.IO.File.Create("c:/jpg/" + (subTexturePic + l) + ".png"))
							{
								t.SaveAsPng(stream, t.Width, t.Height);
							}
							 * */


							var t2 = TextureManager.Rotate(t, RotateFlipType.Rotate180FlipNone);

							TextureManager.Textures.Add(new TextureData((subTexturePic /*baseTexturePic*/+ l /*textureIndex*/), t2));

						}
					}
					//	gameGraphics.unpackImageData(baseTexturePic, abyte3, abyte1, 1);
					//	gameGraphics.drawPicture(0, 0, baseTexturePic);
				}


			}

			TextureManager.GenerateTextureAtlas();

			return true;
		}

		public static void LoadMap()
		{
			if (GameGraphics == null) GameGraphics = new GameImage(512, 346, 4000);
			Engine.mapsFree = UnpackData("maps.jag", "map", 70);
			Engine.mapsMembers = UnpackData("maps.mem", "members map", 75);

			Engine.landscapeFree = UnpackData("land.jag", "landscape", 80);
			Engine.landscapeMembers = UnpackData("land.mem", "members landscape", 85);


		}

		public static bool LoadAnimations()
		{
			if (GameGraphics == null) GameGraphics = new GameImage(512, 346, 4000);

			StringBuilder sb = new StringBuilder();
			sbyte[] abyte0 = null;
			sbyte[] abyte1 = null;
			abyte0 = UnpackData("entity.jag", "people and monsters", 30);
			if (abyte0 == null)
			{
				return false;
			}
			abyte1 = DataOperations.loadData("index.dat", 0, abyte0);
			sbyte[] abyte2 = null;
			sbyte[] abyte3 = null;
			abyte2 = UnpackData("entity.mem", "member graphics", 45);
			if (abyte2 == null)
			{
				return false;
			}
			abyte3 = DataOperations.loadData("index.dat", 0, abyte2);
			int l = 0;
			var animationNumber = 0;
			//label0:
			for (int i1 = 0; i1 < Data.animationCount; i1++)
			{
				//   label4:
				bool breakThis = false;
				String s1 = Data.animationName[i1];
				for (int j1 = 0; j1 < i1; j1++)
				{
					if (!Data.animationName[j1].ToLower().Equals(s1))
						continue;
					Data.animationNumber[i1] = Data.animationNumber[j1];

					// i1++;
					// goto label0;
					//break;
					breakThis = true;
					break;

				}
				if (breakThis) continue;

				//label4:
				sbyte[] abyte7 = DataOperations.loadData(s1 + ".dat", 0, abyte0);
				sbyte[] abyte4 = abyte1;
				if (abyte7 == null)
				{
					abyte7 = DataOperations.loadData(s1 + ".dat", 0, abyte2);
					abyte4 = abyte3;
				}
				if (abyte7 != null)
				{
					try
					{
						GameGraphics.unpackImageData(animationNumber, abyte7, abyte4, 15);
						l += 15;
						if (Data.animationHasA[i1] == 1)
						{
							sbyte[] abyte8 = DataOperations.loadData(s1 + "a.dat", 0, abyte0);
							sbyte[] abyte5 = abyte1;
							if (abyte8 == null)
							{
								abyte8 = DataOperations.loadData(s1 + "a.dat", 0, abyte2);
								abyte5 = abyte3;
							}
							GameGraphics.unpackImageData(animationNumber + 15, abyte8, abyte5, 3);
							l += 3;
						}
						if (Data.animationHasF[i1] == 1)
						{
							sbyte[] abyte9 = DataOperations.loadData(s1 + "f.dat", 0, abyte0);
							sbyte[] abyte6 = abyte1;
							if (abyte9 == null)
							{
								abyte9 = DataOperations.loadData(s1 + "f.dat", 0, abyte2);
								abyte6 = abyte3;
							}
							GameGraphics.unpackImageData(animationNumber + 18, abyte9, abyte6, 9);
							l += 9;
						}
						if (Data.animationGenderModels[i1] != 0)
						{
							for (int k1 = animationNumber; k1 < animationNumber + 27; k1++)
								GameGraphics.loadImage(k1, true);

						}
					}
					catch { }
				}
				Data.animationNumber[i1] = animationNumber;
				animationNumber += 27;
				sb.AppendLine("Loaded: " + l + " frames of animation");

				/* #warning ugly fix for forcing animation count to 1143.
				if (l == 1143) break;
				*/
			endOfLoop: { }
			}
			var str = sb.ToString();
			Debug.Log("Loaded: " + l + " frames of animation");
			return true;
		}

		public static bool LoadMedia() //Sprites
		{
			if (GameGraphics == null) GameGraphics = new GameImage(512, 346, 4000);

			sbyte[] media = UnpackData("media.jag", "2d graphics", 20);
			if (media == null)
			{
				return false;
			}


			baseInventoryPic = 2000;
			baseScrollPic = baseInventoryPic + 100;
			baseItemPicture = baseScrollPic + 50;
			baseLoginScreenBackgroundPic = baseItemPicture + 1000;
			baseProjectilePic = baseLoginScreenBackgroundPic + 10;
			baseTexturePic = baseProjectilePic + 50;
			subTexturePic = baseTexturePic + 10;

			sbyte[] abyte1 = DataOperations.loadData("index.dat", 0, media);
			GameGraphics.unpackImageData(baseInventoryPic, DataOperations.loadData("inv1.dat", 0, media), abyte1, 1);
			GameGraphics.unpackImageData(baseInventoryPic + 1, DataOperations.loadData("inv2.dat", 0, media), abyte1, 6);
			GameGraphics.unpackImageData(baseInventoryPic + 9, DataOperations.loadData("bubble.dat", 0, media), abyte1, 1);
			GameGraphics.unpackImageData(baseInventoryPic + 10, DataOperations.loadData("runescape.dat", 0, media), abyte1, 1);
			GameGraphics.unpackImageData(baseInventoryPic + 11, DataOperations.loadData("splat.dat", 0, media), abyte1, 3);
			GameGraphics.unpackImageData(baseInventoryPic + 14, DataOperations.loadData("icon.dat", 0, media), abyte1, 8);
			GameGraphics.unpackImageData(baseInventoryPic + 22, DataOperations.loadData("hbar.dat", 0, media), abyte1, 1);
			GameGraphics.unpackImageData(baseInventoryPic + 23, DataOperations.loadData("hbar2.dat", 0, media), abyte1, 1);
			GameGraphics.unpackImageData(baseInventoryPic + 24, DataOperations.loadData("compass.dat", 0, media), abyte1, 1);
			GameGraphics.unpackImageData(baseInventoryPic + 25, DataOperations.loadData("buttons.dat", 0, media), abyte1, 2);
			GameGraphics.unpackImageData(baseScrollPic, DataOperations.loadData("scrollbar.dat", 0, media), abyte1, 2);
			GameGraphics.unpackImageData(baseScrollPic + 2, DataOperations.loadData("corners.dat", 0, media), abyte1, 4);
			GameGraphics.unpackImageData(baseScrollPic + 6, DataOperations.loadData("arrows.dat", 0, media), abyte1, 2);
			GameGraphics.unpackImageData(baseProjectilePic, DataOperations.loadData("projectile.dat", 0, media), abyte1, Data.spellProjectileCount);

			int l = Data.highestLoadedPicture;
			for (int i1 = 1; l > 0; i1++)
			{
				int j1 = l;
				l -= 30;
				if (j1 > 30)
					j1 = 30;
				GameGraphics.unpackImageData(baseItemPicture + (i1 - 1) * 30, DataOperations.loadData("objects" + i1 + ".dat", 0, media), abyte1, j1);

			}

			//GameGraphics.UpdateGameImage();
			GameGraphics.loadImage(baseInventoryPic);
			GameGraphics.loadImage(baseInventoryPic + 9);
			for (int k1 = 10 /*11*/; k1 <= /* 26 */ 2000; k1++)
				GameGraphics.loadImage(baseInventoryPic + k1);


			/*
			for (int l1 = 0; l1 < Data.spellProjectileCount; l1++)
				GameGraphics.loadImage(baseProjectilePic + l1);

			for (int i2 = 0; i2 < Data.highestLoadedPicture; i2++)			
				GameGraphics.loadImage(baseProjectilePic + i2);
			*/

			return true;

		}

		public static void TryAddSprite(int pictureIndex)
		{
			var img = FromPicture(pictureIndex);
			if (img != null)
				LoadedSprites.Add(pictureIndex, img);
		}

		public static void TryAddIcon(int pictureIndex)
		{
			var img = FromPicture(pictureIndex);
			if (img != null)
				LoadedIcons.Add(pictureIndex, img);
		}

		public static Texture2D FromPicture(int pictureIndex)
		{
			var pixels = GameGraphics.pictureColors[pictureIndex];
			var width = GameGraphics.pictureWidth[pictureIndex];
			var height = GameGraphics.pictureHeight[pictureIndex];
			if (width == 0 || height == 0)
				return null;



			var bmp = new Texture2D(width, height, TextureFormat.ARGB32, false);
			{

				//bmp.SetPixels(FromInts(pixels.Reverse().ToArray()));
				pixels = pixels.Reverse().ToArray();

				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						var color = pixels[x + y * width];

						var r = (color >> 16) & 0xff;
						var g = (color >> 8) & 0xff;
						var b = color & 0xff;

						if (color == 0)
							bmp.SetPixel(x, y, new Color(0, 0, 0, 0));
						else
							bmp.SetPixel(x, y, new Color(r / 255f, g / 255f, b / 255f, 1f));
					}
				}
				bmp.Apply();

				return bmp;
			}
		}

		public static Color[] FromInts(int[] pixels)
		{
			List<Color> colors = new List<Color>();
			for (int i = 0; i < pixels.Length; i++)
			{
				var color = pixels[i];

				var r = (color >> 16) & 0xff;
				var g = (color >> 8) & 0xff;
				var b = color & 0xff;

				if (color == 0)
					colors.Add(new Color(0, 0, 0, 0));
				else
					colors.Add(new Color(b, r, g));
			}
			return colors.ToArray();
		}


		public static Texture2D FromGamePixels()
		{
			var pixels = GameGraphics.pixels;
			var width = GameGraphics.gameWidth;//width;
			var height = GameGraphics.gameHeight;//.height;

			if (width == 0 || height == 0)
				return null;

			//uint[] colors = new uint[GameGraphics.pixels.Length];
			//for (int j = 0; j < GameGraphics.pixels.Length; j++)
			//{
			//	var bytes = BitConverter.GetBytes(GameGraphics.pixels[j]);
			//	var r = bytes[2];
			//	var g = bytes[1];
			//	var b = bytes[0];

			//	colors[j] = GameImage.rgbaToUInt(r, g, b, 255);//new Color(r, g, b, 255).PackedValue;                            
			//	//colors.Add();
			//}


			var bmp = new Texture2D(width, height, TextureFormat.ARGB32, false);
			{
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						var color = pixels[x + y * width];

						var r = (color >> 16) & 0xff;
						var g = (color >> 8) & 0xff;
						var b = color & 0xff;
						/*
						if (color == 0)
							bmp.SetPixel(x, y, Color.Transparent);
						else*/
						bmp.SetPixel(x, y, new Color(r / 255f, g / 255f, b / 255f, 1f));
					}
				}
				bmp.Apply();

				return bmp;
			}
		}


		public static sbyte[] UnpackData(String arg0, String arg1, int arg2)
		{
			sbyte[] abyte0 = link.getFile(arg0);
			if (abyte0 != null)
			{
				//	Debug.Log(arg0 + " loaded");

				int l = ((abyte0[0] & 0xff) << 16) + ((abyte0[1] & 0xff) << 8) + (abyte0[2] & 0xff);
				int i1 = ((abyte0[3] & 0xff) << 16) + ((abyte0[4] & 0xff) << 8) + (abyte0[5] & 0xff);

				sbyte[] abyte1 = new sbyte[abyte0.Length - 6];
				for (int j1 = 0; j1 < abyte0.Length - 6; j1++)
					abyte1[j1] = abyte0[j1 + 6];

				//	drawLoadingBarText(arg2, "Unpacking " + arg1);
				if (i1 != l)
				{
					sbyte[] abyte2 = new sbyte[l];
					DataFileDecrypter.unpackData(abyte2, l, abyte1, i1, 0);
					//if (OnContentLoaded != null)
					//{
					//	OnContentLoaded(this, new ContentLoadedEventArgs("Unpacking " + arg1, arg2));
					//}
					return abyte2;
				}
				else
				{
					//if (OnContentLoaded != null)
					//{
					//	OnContentLoaded(this, new ContentLoadedEventArgs("Unpacking " + arg1, arg2));
					//}
					return abyte1;
				}
			}
			else
			{
				//if (OnContentLoaded != null)
				//{
				//	OnContentLoaded(this, new ContentLoadedEventArgs("Unpacking " + arg1, arg2));
				//}
				return UnpackDataBase(arg0, arg1, arg2);
			}
		}

		public static sbyte[] UnpackDataBase(String filename, String fileTitle, int startPercentage)
		{

			Debug.Log("Using default load");
			int i = 0;
			int k = 0;
			sbyte[] abyte0 = link.getFile(filename);
			if (abyte0 == null)
			{
				try
				{
					Debug.Log("Loading " + fileTitle + " - 0%");
					// drawLoadingBarText(startPercentage, "Loading " + fileTitle + " - 0%");
					var inputstream = new BinaryReader(DataOperations.openInputStream(filename));
					//DataInputStream datainputstream = new DataInputStream(inputstream);
					sbyte[] abyte2 = new sbyte[6] {
                        inputstream.ReadSByte(),inputstream.ReadSByte(),inputstream.ReadSByte(),
                        inputstream.ReadSByte(),inputstream.ReadSByte(),inputstream.ReadSByte()
                    };

					//inputstream.Read(abyte2, 0, 6);
					i = ((abyte2[0] & 0xff) << 16) + ((abyte2[1] & 0xff) << 8) + (abyte2[2] & 0xff);
					k = ((abyte2[3] & 0xff) << 16) + ((abyte2[4] & 0xff) << 8) + (abyte2[5] & 0xff);



					Debug.Log("Loading " + fileTitle + " - 5%");
					// drawLoadingBarText(startPercentage, "Loading " + fileTitle + " - 5%");

					// int l = 0;
					int l = 6;
					abyte0 = new sbyte[k];
					while (l < k)
					{
						int i1 = k - l;
						if (i1 > 1000)
							i1 = 1000;

						for (int t = 0; t < i1; t++)
							abyte0[l + t] = inputstream.ReadSByte();

						// inputstream.Read(abyte0, l, i1);

						l += i1;
						Debug.Log("Loading " + fileTitle + " - " + (5 + (l * 95) / k) + "%");
						//	drawLoadingBarText(startPercentage, "Loading " + fileTitle + " - " + (5 + (l * 95) / k) + "%");
					}

					inputstream.Close();
				}
				catch (IOException _ex) { }
			}

			Debug.Log("Unpacking " + fileTitle);
			//drawLoadingBarText(startPercentage, "Unpacking " + fileTitle);
			if (k != i)
			{
				sbyte[] abyte1 = new sbyte[i];
				DataFileDecrypter.unpackData(abyte1, i, abyte0, k, 0);
				return abyte1;
			}
			else
			{
				//  return unpackData(filename, fileTitle, startPercentage); // abyte0;
				return abyte0;
			}
		}


	}
}
