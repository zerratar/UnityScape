using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
namespace Assets.RSC.Models
{
	using System.Diagnostics;
	using System.IO;
	using Assets.RSC.IO;
	using Assets.RSC.Managers;
	using UnityEngine;
	using Random = System.Random;

	public class Engine
	{

		public static sbyte[] mapsFree { get; set; }

		public static sbyte[] mapsMembers { get; set; }

		public static sbyte[] landscapeFree { get; set; }

		public static sbyte[] landscapeMembers { get; set; }

		private static int SECTOR_COUNT = 4;

		public static ModelData Terrain, Roof, Wall, Floor;

		public static int EntityIndex_Tile = 200000;
		public static int EntityIndex_Wall = 30000;

		public static int NULLINTEGER = 12345678;

		static Engine()
		{
			Terrain = new ModelData("RSCTerrain");
			Roof = new ModelData("RSCRoof");
			Wall = new ModelData("RSCWall");
			Floor = new ModelData("RSCFloor");

			//new org.moparscape.msc.client.EngineHandle
			int o2 = 2304;
			int o9 = 96;
			int o6 = 64;

			tileHorizontalWall = new int[SECTOR_COUNT][];
			tileDiagonalWall = new int[SECTOR_COUNT][];
			tileGroundOverlay = new int[SECTOR_COUNT][];
			tileObjectRotation = new int[SECTOR_COUNT][];
			tileGroundTexture = new int[SECTOR_COUNT][];
			tileVerticalWall = new int[SECTOR_COUNT][];
			tileGroundElevation = new sbyte[SECTOR_COUNT][];
			tileRoofType = new int[SECTOR_COUNT][];

			wallObject = new GameObject[SECTOR_COUNT][];
			roofObject = new GameObject[SECTOR_COUNT][];

			for (int j = 0; j < 4; j++)
			{
				tileHorizontalWall[j] = new int[o2];
				tileDiagonalWall[j] = new int[o2];
				tileGroundOverlay[j] = new int[o2];
				tileObjectRotation[j] = new int[o2];
				tileGroundTexture[j] = new int[o2];
				tileVerticalWall[j] = new int[o2];
				tileGroundElevation[j] = new sbyte[o2];
				tileRoofType[j] = new int[o2];

				wallObject[j] = new GameObject[o6];
				roofObject[j] = new GameObject[o6];
			}

			roofTileHeight = new int[o9][];
			tiles = new int[o9][];
			steps = new int[o9][];
			objectDirs = new int[o9][];

			for (int j = 0; j < o9; j++)
			{
				roofTileHeight[j] = new int[o9];
				tiles[j] = new int[o9];
				steps[j] = new int[o9];
				objectDirs[j] = new int[o9];
			}


			//ghh = false;
			selectedY = new int[18432];
			groundTexture = new int[256];
			GroundTextureColor = new Color[256];
			TileChunks = new GameObject[64];

			playerIsAlive = false;

			selectedX = new int[18432];


			//gjb = true;
			//	baseInventoryPic = 750;
			//	_camera = arg0;
			// 	GameGraphics = arg1;
			for (int k = 0; k < 64; k++)
			{

				groundTexture[k] = Camera.ToTextureColor(255 - k * 4, 255 - (int)((double)k * 1.75D), 255 - k * 4);
				GroundTextureColor[k] = new Color(255f - k * 4, (float)(255f - ((double)k * 1.75D)), 255f - k * 4);

			}
			for (int l = 0; l < 64; l++)
			{
				groundTexture[l + 64] = Camera.ToTextureColor(l * 3, 144, 0);
				GroundTextureColor[l + 64] = new Color(l * 3, 144, 0);
			}
			for (int i1 = 0; i1 < 64; i1++)
			{
				groundTexture[i1 + 128] = Camera.ToTextureColor(192 - (int)((double)i1 * 1.5D), 144 - (int)((double)i1 * 1.5D), 0);
				GroundTextureColor[i1 + 128] = new Color(192 - (int)((double)i1 * 1.5D), 144 - (int)((double)i1 * 1.5D), 0);

			}
			for (int j1 = 0; j1 < 64; j1++)
			{
				groundTexture[j1 + 192] = Camera.ToTextureColor(96 - (int)((double)j1 * 1.5D), 48 + (int)((double)j1 * 1.5D), 0);
				GroundTextureColor[j1 + 192] = new Color(96 - (int)((double)j1 * 1.5D), 48 + (int)((double)j1 * 1.5D), 0);

			}

			for (int j = 0; j < GroundTextureColor.Length; j++)
			{
				GroundTextureColor[j] = new Color(GroundTextureColor[j].r / 255f, GroundTextureColor[j].g / 255f, GroundTextureColor[j].b / 255f);
				// UnityEngine.Debug.Log(GroundTextureColor[j]);
			}

		}


		public static int getTileGroundTextureIndex(int x, int y)
		{
			if (x < 0 || x >= 96 || y < 0 || y >= 96)
				return 0;
			byte byte0 = 0;
			if (x >= 48 && y < 48)
			{
				byte0 = 1;
				x -= 48;
			}
			else if (x < 48 && y >= 48)
			{
				byte0 = 2;
				y -= 48;
			}
			else if (x >= 48 && y >= 48)
			{
				byte0 = 3;
				x -= 48;
				y -= 48;
			}
			return tileGroundTexture[byte0][x * 48 + y] & 0xff;
		}

		public static int getTileElevation(int arg0, int arg1)
		{
			if (arg0 < 0 || arg0 >= 96 || arg1 < 0 || arg1 >= 96)
				return 0;
			sbyte byte0 = 0;
			if (arg0 >= 48 && arg1 < 48)
			{
				byte0 = 1;
				arg0 -= 48;
			}
			else if (arg0 < 48 && arg1 >= 48)
			{
				byte0 = 2;
				arg1 -= 48;
			}
			else if (arg0 >= 48 && arg1 >= 48)
			{
				byte0 = 3;
				arg0 -= 48;
				arg1 -= 48;
			}
//#warning /*0xff*/
			return (tileGroundElevation[byte0][arg0 * 48 + arg1] & 0xff) * 3;
		}

		public static int getAveragedElevation(int x, int y_or_z)
		{
			int k = x >> 7;
			int l = y_or_z >> 7;
			int i1 = x & 0x7f;
			int j1 = y_or_z & 0x7f;
			if (k < 0 || l < 0 || k >= 95 || l >= 95)
				return 0;
			int k1;
			int l1;
			int i2;
			if (i1 <= 128 - j1)
			{
				k1 = getTileElevation(k, l);
				l1 = getTileElevation(k + 1, l) - k1;
				i2 = getTileElevation(k, l + 1) - k1;
			}
			else
			{
				k1 = getTileElevation(k + 1, l + 1);
				l1 = getTileElevation(k, l + 1) - k1;
				i2 = getTileElevation(k + 1, l) - k1;
				i1 = 128 - i1;
				j1 = 128 - j1;
			}
			int j2 = k1 + (l1 * i1) / 128 + (i2 * j1) / 128;
			return j2;
		}

		internal static float[,] HeightMap()
		{
			var Size = 48;
			var HeightData = new float[Size * 2, Size * 2];
			var TerrainHeight = Size * 2;
			var TerrainWidth = Size * 2;

			//RscLandscapeCache.Sections

			for (int x = 0; x < 96; x++)
			{
				for (int y = 0; y < 96; y++)
				{
					//var lane = RscLandscapeCache.GetLane(x, y);
					HeightData[x, y] = getTileElevation(x, y) * 0.0001f; // RscLandscapeCache.Sections[lane].RscTiles[x * 48 + y].groundElevation;
				}
			}
			return HeightData;
		}

		public static int ToTextureColor(int r, int g, int b)
		{
			return -1 - (r / 8) * 1024 - (g / 8) * 32 - b / 8;
		}

		public static void cleanUpWorld()
		{
			// if (gjb)
			//	_camera.cleanUp();
			for (int k = 0; k < 64; k++)
			{
				TileChunks[k] = null;
				for (int l = 0; l < 4; l++)
					wallObject[l][k] = null;

				for (int i1 = 0; i1 < 4; i1++)
					roofObject[i1][k] = null;

			}

			//System.gc();
			// GARBAGE COLLECT
			System.GC.Collect();

		}


		public static int getTileGroundOverlayIndex(int x, int y, int height)
		{
			if (x < 0 || x >= 96 || y < 0 || y >= 96)
				return 0;
			byte byte0 = 0;
			if (x >= 48 && y < 48)
			{
				byte0 = 1;
				x -= 48;
			}
			else if (x < 48 && y >= 48)
			{
				byte0 = 2;
				y -= 48;
			}
			else if (x >= 48 && y >= 48)
			{
				byte0 = 3;
				x -= 48;
				y -= 48;
			}
			return tileGroundOverlay[byte0][x * 48 + y] & 0xff;
		}

		public static int getTile(int x, int y)
		{
			if (x < 0 || y < 0 || x >= 96 || y >= 96)
				return 0;
			else
				return tiles[x][y];
		}


		public static void setTileGroundOverlayHeight(int x, int y, int height)
		{
			if (x < 0 || x >= 96 || y < 0 || y >= 96)
				return;
			byte layer = 0;
			if (x >= 48 && y < 48)
			{
				layer = 1;
				x -= 48;
			}
			else if (x < 48 && y >= 48)
			{
				layer = 2;
				y -= 48;
			}
			else if (x >= 48 && y >= 48)
			{
				layer = 3;
				x -= 48;
				y -= 48;
			}
			tileGroundOverlay[layer][x * 48 + y] = height;
		}

		public void gka(int x, int y, int objWidth, int objHeight)
		{
			if (x < 1 || y < 1 || x + objWidth >= 96 || y + objHeight >= 96)
				return;
			for (int x1 = x; x1 <= x + objWidth; x1++)
			{
				for (int y1 = y; y1 <= y + objHeight; y1++)
					if ((getTile(x1, y1) & 0x63) != 0 || (getTile(x1 - 1, y1) & 0x59) != 0 || (getTile(x1, y1 - 1) & 0x56) != 0 || (getTile(x1 - 1, y1 - 1) & 0x6c) != 0)
						SetTileType(x1, y1, 35);
					else
						SetTileType(x1, y1, 0);

			}

		}

		public static void updateTileChunk(int objectX, int objectY, int x, int y, int val)
		{
			// GameObject tileChunk = TileChunks[objectX + objectY * 8];
			var tileChunk = Terrain;

			if (tileChunk != null)
			{
				for (int vertIndex = 0; vertIndex < tileChunk.Vertices.Count; vertIndex++)
					if (tileChunk.Vertices[vertIndex].Position.x == x * 128 && tileChunk.Vertices[vertIndex].Position.z == y * 128)
					{
						tileChunk.SetVertexColor(vertIndex, val);
						return;
					}
			}
		}

		public static void SetTileType(int x, int y, int i1)
		{
			int j1 = x / 12;
			int k1 = y / 12;
			int l1 = (x - 1) / 12;
			int i2 = (y - 1) / 12;
			updateTileChunk(j1, k1, x, y, i1);
			if (j1 != l1)
				updateTileChunk(l1, k1, x, y, i1);
			if (k1 != i2)
				updateTileChunk(j1, i2, x, y, i1);
			if (j1 != l1 && k1 != i2)
				updateTileChunk(l1, i2, x, y, i1);
		}


		public static void stitchAreaTileColors()
		{
			for (int x = 0; x < 96; x++)
			{
				for (int y = 0; y < 96; y++)
				{
					if (getTileGroundOverlayIndex(x, y, 0) == 250)
					{
						if (x == 47 && getTileGroundOverlayIndex(x + 1, y, 0) != 250 && getTileGroundOverlayIndex(x + 1, y, 0) != 2)
							setTileGroundOverlayHeight(x, y, 9);
						else if (y == 47 && getTileGroundOverlayIndex(x, y + 1, 0) != 250 && getTileGroundOverlayIndex(x, y + 1, 0) != 2)
							setTileGroundOverlayHeight(x, y, 9);
						else
							setTileGroundOverlayHeight(x, y, 2);
					}
				}

			}

		}
		public static int getTileGroundOverlayTextureOrDefault(int x, int y, int height, int defaultTexture)
		{
			int k1 = getTileGroundOverlayIndex(x, y, height);
			if (k1 == 0)
				return defaultTexture;
			else
				return Data.TileGroundOverlayTexture[k1 - 1];
		}
		public static int getVerticalWall(int x, int y)
		{
			if (x < 0 || x >= 96 || y < 0 || y >= 96)
				return 0;
			byte layer = 0;
			if (x >= 48 && y < 48)
			{
				layer = 1;
				x -= 48;
			}
			else if (x < 48 && y >= 48)
			{
				layer = 2;
				y -= 48;
			}
			else if (x >= 48 && y >= 48)
			{
				layer = 3;
				x -= 48;
				y -= 48;
			}
			return tileVerticalWall[layer][x * 48 + y] & 0xff;
		}

		public static int getHorizontalWall(int x, int y)
		{
			if (x < 0 || x >= 96 || y < 0 || y >= 96)
				return 0;
			byte layer = 0;
			if (x >= 48 && y < 48)
			{
				layer = 1;
				x -= 48;
			}
			else if (x < 48 && y >= 48)
			{
				layer = 2;
				y -= 48;
			}
			else if (x >= 48 && y >= 48)
			{
				layer = 3;
				x -= 48;
				y -= 48;
			}

			return tileHorizontalWall[layer][x * 48 + y] & 0xff;
		}

		public static int getDiagonalWall(int x, int y)
		{
			if (x < 0 || x >= 96 || y < 0 || y >= 96)
				return 0;
			byte layer = 0;
			if (x >= 48 && y < 48)
			{
				layer = 1;
				x -= 48;
			}
			else if (x < 48 && y >= 48)
			{
				layer = 2;
				y -= 48;
			}
			else if (x >= 48 && y >= 48)
			{
				layer = 3;
				x -= 48;
				y -= 48;
			}

			return tileDiagonalWall[layer][x * 48 + y];
		}

		public static int getTileType2(int x, int y, int height)
		{
			int j1 = getTileGroundOverlayIndex(x, y, height);
			if (j1 == 0)
				return -1;
			int k1 = Data.tileGroundOverlayTypes[j1 - 1];
			return k1 != 2 ? 0 : 1;
		}


		public static System.Random Ran = new Random();

		public static void loadSection(int x, int y, int height, bool freshLoad)
		{

			int sectionX = (x + 24) / 48;
			int sectionY = (y + 24) / 48;
			loadSection(sectionX - 1, sectionY - 1, height, 0);
			loadSection(sectionX, sectionY - 1, height, 1);
			loadSection(sectionX - 1, sectionY, height, 2);
			loadSection(sectionX, sectionY, height, 3);
			stitchAreaTileColors();
			// if (currentSectionObject == null) currentSectionObject = new GameObject(18688, 18688, true, true, false, false, true);
			if (freshLoad)
			{
				// gameGraphics.clearScreen();
				for (int x1 = 0; x1 < 96; x1++)
				{
					for (int y1 = 0; y1 < 96; y1++) tiles[x1][y1] = 0;
				}

				Terrain.Reset();

				for (int j2 = 0; j2 < 96; j2++)
				{
					for (int i3 = 0; i3 < 96; i3++)
					{
						//int i4 = -getTileElevation(j2, i3);
						int i4 = -getTileElevation(j2, i3);
						if (getTileGroundOverlayIndex(j2, i3, height) > 0
							&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(j2, i3, height) - 1] == 4) i4 = 0;
						if (getTileGroundOverlayIndex(j2 - 1, i3, height) > 0
							&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(j2 - 1, i3, height) - 1] == 4) i4 = 0;
						if (getTileGroundOverlayIndex(j2, i3 - 1, height) > 0
							&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(j2, i3 - 1, height) - 1] == 4) i4 = 0;
						if (getTileGroundOverlayIndex(j2 - 1, i3 - 1, height) > 0
							&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(j2 - 1, i3 - 1, height) - 1] == 4) i4 = 0;
						int vertexIndex = Terrain.getVertexIndex(j2 * 128, i4, i3 * 128);
						int color = (int)(Ran.NextDouble() * 10D) - 5; //Helper.Random.NextDouble()
						Terrain.SetVertexColor(vertexIndex, color);
					}

				}

				for (int x1 = 0; x1 < 95; x1++) //< 95
				{
					for (int y1 = 0; y1 < 95; y1++) //< 95
					{
						int tileTextureIndex = getTileGroundTextureIndex(x1, y1);
						int texture = groundTexture[tileTextureIndex];
						int texture1 = texture;
						int texture2 = texture;
						int tileAngleValue = 0;
						if (height == 1 || height == 2)
						{
							texture = NULLINTEGER;
							texture1 = NULLINTEGER;
							texture2 = NULLINTEGER;
						}
						if (getTileGroundOverlayIndex(x1, y1, height) > 0)
						{
							int tileIndex = getTileGroundOverlayIndex(x1, y1, height);
							int tileType = Data.tileGroundOverlayTypes[tileIndex - 1];
							int i19 = getTileType2(x1, y1, height);
							texture = texture1 = Data.TileGroundOverlayTexture[tileIndex - 1];
							if (tileType == 4)
							{
								texture = 1;
								texture1 = 1;
								if (tileIndex == 12)
								{
									texture = 31;
									texture1 = 31;
								}
							}
							if (tileType == 5)
							{
								if (getDiagonalWall(x1, y1) > 0 && getDiagonalWall(x1, y1) < 24000)
									if (getTileGroundOverlayTextureOrDefault(x1 - 1, y1, height, texture2) != NULLINTEGER
										&& getTileGroundOverlayTextureOrDefault(x1, y1 - 1, height, texture2) != NULLINTEGER)
									{
										texture = getTileGroundOverlayTextureOrDefault(x1 - 1, y1, height, texture2);
										tileAngleValue = 0;
									}
									else if (getTileGroundOverlayTextureOrDefault(x1 + 1, y1, height, texture2) != NULLINTEGER
											&& getTileGroundOverlayTextureOrDefault(x1, y1 + 1, height, texture2) != NULLINTEGER)
									{
										texture1 = getTileGroundOverlayTextureOrDefault(x1 + 1, y1, height, texture2);
										tileAngleValue = 0;
									}
									else if (getTileGroundOverlayTextureOrDefault(x1 + 1, y1, height, texture2) != NULLINTEGER
											&& getTileGroundOverlayTextureOrDefault(x1, y1 - 1, height, texture2) != NULLINTEGER)
									{
										texture1 = getTileGroundOverlayTextureOrDefault(x1 + 1, y1, height, texture2);
										tileAngleValue = 1;
									}
									else if (getTileGroundOverlayTextureOrDefault(x1 - 1, y1, height, texture2) != NULLINTEGER
											&& getTileGroundOverlayTextureOrDefault(x1, y1 + 1, height, texture2) != NULLINTEGER)
									{
										texture = getTileGroundOverlayTextureOrDefault(x1 - 1, y1, height, texture2);
										tileAngleValue = 1;
									}
							}
							else if (tileType != 2 || getDiagonalWall(x1, y1) > 0 && getDiagonalWall(x1, y1) < 24000)
								if (getTileType2(x1 - 1, y1, height) != i19 && getTileType2(x1, y1 - 1, height) != i19)
								{
									texture = texture2;
									tileAngleValue = 0;
								}
								else if (getTileType2(x1 + 1, y1, height) != i19 && getTileType2(x1, y1 + 1, height) != i19)
								{
									texture1 = texture2;
									tileAngleValue = 0;
								}
								else if (getTileType2(x1 + 1, y1, height) != i19 && getTileType2(x1, y1 - 1, height) != i19)
								{
									texture1 = texture2;
									tileAngleValue = 1;
								}
								else if (getTileType2(x1 - 1, y1, height) != i19 && getTileType2(x1, y1 + 1, height) != i19)
								{
									texture = texture2;
									tileAngleValue = 1;
								}

							if (Data.tileWalkable[tileIndex - 1] != 0) tiles[x1][y1] |= 0x40;
							if (Data.tileGroundOverlayTypes[tileIndex - 1] == 2) tiles[x1][y1] |= 0x80;
							// Drawminimappixel here
						}
						int tileSlope = ((getTileElevation(x1 + 1, y1 + 1) - getTileElevation(x1 + 1, y1)) + getTileElevation(x1, y1 + 1))
										- getTileElevation(x1, y1);
						if (texture != texture1 || tileSlope != 0)
						{
							int[] indices = new int[3];
							int[] indices2 = new int[3];
							if (tileAngleValue == 0)
							{
								if (texture != NULLINTEGER)
								{
									indices[0] = y1 + x1 * 96 + 96;
									indices[1] = y1 + x1 * 96;
									indices[2] = y1 + x1 * 96 + 1;

									int objIndex = Terrain.AddFace(3, indices, NULLINTEGER, texture);
									selectedX[objIndex] = x1;
									selectedY[objIndex] = y1;
									Terrain.entityType[objIndex] = EntityIndex_Tile + objIndex;
								}
								if (texture1 != NULLINTEGER)
								{
									indices2[0] = y1 + x1 * 96 + 1;
									indices2[1] = y1 + x1 * 96 + 96 + 1;
									indices2[2] = y1 + x1 * 96 + 96;

									int objIndex = Terrain.AddFace(3, indices2, NULLINTEGER, texture1);
									selectedX[objIndex] = x1;
									selectedY[objIndex] = y1;
									Terrain.entityType[objIndex] = EntityIndex_Tile + objIndex;
								}
							}
							else
							{
								if (texture != NULLINTEGER)
								{
									indices[0] = y1 + x1 * 96 + 1;
									indices[1] = y1 + x1 * 96 + 96 + 1;
									indices[2] = y1 + x1 * 96;

									int objIndex = Terrain.AddFace(3, indices, NULLINTEGER, texture);
									selectedX[objIndex] = x1;
									selectedY[objIndex] = y1;
									Terrain.entityType[objIndex] = EntityIndex_Tile + objIndex;
								}
								if (texture1 != NULLINTEGER)
								{
									indices2[0] = y1 + x1 * 96 + 96;
									indices2[1] = y1 + x1 * 96;
									indices2[2] = y1 + x1 * 96 + 96 + 1;

									int objIndex = Terrain.AddFace(3, indices2, NULLINTEGER, texture1);
									selectedX[objIndex] = x1;
									selectedY[objIndex] = y1;
									Terrain.entityType[objIndex] = EntityIndex_Tile + objIndex;
								}
							}
						}
						else if (texture != NULLINTEGER)
						{


							// if (texture != NULLINTEGER)

							int[] indices = new int[3];
							indices[0] = y1 + x1 * 96 + 96;
							indices[1] = y1 + x1 * 96;
							indices[2] = y1 + x1 * 96 + 1;

							var objIndex = Terrain.AddFace(3, indices, NULLINTEGER, texture);
							selectedX[objIndex] = x1;
							selectedY[objIndex] = y1;
							Terrain.entityType[objIndex] = EntityIndex_Tile + objIndex;

							//if (texture1 != NULLINTEGER)

							indices = new int[3];
							indices[0] = y1 + x1 * 96 + 1;
							indices[1] = y1 + x1 * 96 + 96 + 1;
							indices[2] = y1 + x1 * 96 + 96;

							objIndex = Terrain.AddFace(3, indices, NULLINTEGER, texture);
							selectedX[objIndex] = x1;
							selectedY[objIndex] = y1;
							Terrain.entityType[objIndex] = EntityIndex_Tile + objIndex;

						}
					}
				}


				for (int x1 = 1; x1 < 95; x1++)
				{
					for (int y1 = 1; y1 < 95; y1++)
						if (getTileGroundOverlayIndex(x1, y1, height) > 0
							&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(x1, y1, height) - 1] == 4)
						{
							int l7 = Data.TileGroundOverlayTexture[getTileGroundOverlayIndex(x1, y1, height) - 1];
							int a1 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1), y1 * 128);
							int a2 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1), y1 * 128);
							int a3 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1 + 1), (y1 + 1) * 128);
							int a4 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1 + 1), (y1 + 1) * 128);

							int[] ai2 = { a1, a2, a3 };
							int i20 = Terrain.AddFace(3, ai2, l7, NULLINTEGER);
							selectedX[i20] = x1;
							selectedY[i20] = y1;
							Terrain.entityType[i20] = EntityIndex_Tile + i20;

							int[] ai3 = { a3, a1, a4 };
							i20 = Terrain.AddFace(3, ai3, l7, NULLINTEGER);
							selectedX[i20] = x1;
							selectedY[i20] = y1;
							Terrain.entityType[i20] = EntityIndex_Tile + i20;

							// drawMinimapPixel(x1, y1, 0, l7, l7);
						}
						else
							if (getTileGroundOverlayIndex(x1, y1, height) == 0
								|| Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(x1, y1, height) - 1] != 3)
							{
								if (getTileGroundOverlayIndex(x1, y1 + 1, height) > 0
									&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(x1, y1 + 1, height) - 1] == 4)
								{
									int i8 = Data.TileGroundOverlayTexture[getTileGroundOverlayIndex(x1, y1 + 1, height) - 1];
									int a1 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1), y1 * 128);
									int a2 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1), y1 * 128);
									int a3 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1 + 1), (y1 + 1) * 128);
									int a4 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1 + 1), (y1 + 1) * 128);

									int[] ai3 = { a1, a2, a3 };
									int[] ai4 = { a3, a1, a4 };

									int j20 = Terrain.AddFace(3, ai3, i8, NULLINTEGER);
									selectedX[j20] = x1;
									selectedY[j20] = y1;
									Terrain.entityType[j20] = EntityIndex_Tile + j20;


									j20 = Terrain.AddFace(3, ai4, i8, NULLINTEGER);
									selectedX[j20] = x1;
									selectedY[j20] = y1;
									Terrain.entityType[j20] = EntityIndex_Tile + j20;


									// drawMinimapPixel(x1, y1, 0, i8, i8);
								}
								if (getTileGroundOverlayIndex(x1, y1 - 1, height) > 0
									&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(x1, y1 - 1, height) - 1] == 4)
								{
									int j8 = Data.TileGroundOverlayTexture[getTileGroundOverlayIndex(x1, y1 - 1, height) - 1];
									int a1 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1), y1 * 128);
									int a2 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1), y1 * 128);
									int a3 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1 + 1), (y1 + 1) * 128);
									int a4 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1 + 1), (y1 + 1) * 128);
									int[] ai4 = new[] { a1, a2, a3 };
									int k20 = Terrain.AddFace(3, ai4, j8, NULLINTEGER);
									selectedX[k20] = x1;
									selectedY[k20] = y1;
									Terrain.entityType[k20] = EntityIndex_Tile + k20;

									int[] ai5 = new[] { a3, a1, a4 };
									k20 = Terrain.AddFace(3, ai5, j8, NULLINTEGER);
									selectedX[k20] = x1;
									selectedY[k20] = y1;
									Terrain.entityType[k20] = EntityIndex_Tile + k20;
									// drawMinimapPixel(x1, y1, 0, j8, j8);
								}
								if (getTileGroundOverlayIndex(x1 + 1, y1, height) > 0
									&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(x1 + 1, y1, height) - 1] == 4)
								{
									int k8 = Data.TileGroundOverlayTexture[getTileGroundOverlayIndex(x1 + 1, y1, height) - 1];
									int a1 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1), y1 * 128);
									int a2 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1), y1 * 128);
									int a3 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1 + 1), (y1 + 1) * 128);
									int a4 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1 + 1), (y1 + 1) * 128);
									var ai5 = new int[] { a1, a2, a3 };
									int l20 = Terrain.AddFace(3, ai5, k8, NULLINTEGER);
									selectedX[l20] = x1;
									selectedY[l20] = y1;
									Terrain.entityType[l20] = EntityIndex_Tile + l20;

									var ai6 = new int[] { a3, a1, a4 };
									l20 = Terrain.AddFace(3, ai6, k8, NULLINTEGER);
									selectedX[l20] = x1;
									selectedY[l20] = y1;
									Terrain.entityType[l20] = EntityIndex_Tile + l20;
									//	drawMinimapPixel(x1, y1, 0, k8, k8);
								}
								if (getTileGroundOverlayIndex(x1 - 1, y1, height) > 0
									&& Data.tileGroundOverlayTypes[getTileGroundOverlayIndex(x1 - 1, y1, height) - 1] == 4)
								{
									int l8 = Data.TileGroundOverlayTexture[getTileGroundOverlayIndex(x1 - 1, y1, height) - 1];
									int a1 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1), y1 * 128);
									int a2 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1), y1 * 128);
									int a3 = Terrain.getVertexIndex((x1 + 1) * 128, -getTileElevation(x1 + 1, y1 + 1), (y1 + 1) * 128);
									int a4 = Terrain.getVertexIndex(x1 * 128, -getTileElevation(x1, y1 + 1), (y1 + 1) * 128);

									int[] ai6 = new[] { a1, a2, a3 };
									int i21 = Terrain.AddFace(3, ai6, l8, NULLINTEGER);
									selectedX[i21] = x1;
									selectedY[i21] = y1;
									Terrain.entityType[i21] = EntityIndex_Tile + i21;

									int[] ai7 = new[] { a3, a1, a4 };
									i21 = Terrain.AddFace(3, ai7, l8, NULLINTEGER);
									selectedX[i21] = x1;
									selectedY[i21] = y1;
									Terrain.entityType[i21] = EntityIndex_Tile + i21;
									// drawMinimapPixel(x1, y1, 0, l8, l8);
								}
							}

				}

				// Reset roofTileHeight
				// This will lower down where the roofs are set
				// They should match the tile elevation (aka. Y) to begin with
				// And then offset from there to gain their true elevation
				for (int i9 = 0; i9 < 96; i9++)
				{
					for (int k11 = 0; k11 < 96; k11++) roofTileHeight[i9][k11] = getTileElevation(i9, k11);
				}

				// }

				bool ghh = false;


				Wall.Reset();

				/* Create Walls */
				int j1 = 0x606060;
				for (int x1 = 0; x1 < 95; x1++)
				{
					for (int y1 = 0; y1 < 95; y1++)
					{
						int k3 = getHorizontalWall(x1, y1);
						if (k3 > 0 && (Data.wallObjectUnknown[k3 - 1] == 0 || ghh))
						{
							makeWall(Wall, k3 - 1, x1, y1, x1 + 1, y1);
							if (freshLoad && Data.wallObjectType[k3 - 1] != 0)
							{
								tiles[x1][y1] |= 1;
								if (y1 > 0) setTileType(x1, y1 - 1, 4);
							}
							//	if (freshLoad)
							//		gameGraphics.drawLineX(x1 * 3, y1 * 3, 3, j1); 
						}
						k3 = getVerticalWall(x1, y1);
						if (k3 > 0 && (Data.wallObjectUnknown[k3 - 1] == 0 || ghh))
						{
							makeWall(Wall, k3 - 1, x1, y1, x1, y1 + 1);
							if (freshLoad && Data.wallObjectType[k3 - 1] != 0)
							{
								tiles[x1][y1] |= 2;
								if (x1 > 0) setTileType(x1 - 1, y1, 8);
							}
							//	if (freshLoad)
							//		gameGraphics.drawLineY(x1 * 3, y1 * 3, 3, j1);
						}
						k3 = getDiagonalWall(x1, y1);
						if (k3 > 0 && k3 < 12000 && (Data.wallObjectUnknown[k3 - 1] == 0 || ghh))
						{
							makeWall(Wall, k3 - 1, x1, y1, x1 + 1, y1 + 1);
							if (freshLoad && Data.wallObjectType[k3 - 1] != 0) tiles[x1][y1] |= 0x20;
							//if (freshLoad)
							//{
							//	gameGraphics.drawMinimapPixel(x1 * 3, y1 * 3, j1);
							//	gameGraphics.drawMinimapPixel(x1 * 3 + 1, y1 * 3 + 1, j1);
							//	gameGraphics.drawMinimapPixel(x1 * 3 + 2, y1 * 3 + 2, j1);
							//}
						}
						if (k3 > 12000 && k3 < 24000 && (Data.wallObjectUnknown[k3 - 12001] == 0 || ghh))
						{
							makeWall(Wall, k3 - 12001, x1 + 1, y1, x1, y1 + 1);
							if (freshLoad && Data.wallObjectType[k3 - 12001] != 0) tiles[x1][y1] |= 0x10;
							//if (freshLoad)
							//{
							//	gameGraphics.drawMinimapPixel(x1 * 3 + 2, y1 * 3, j1);
							//	gameGraphics.drawMinimapPixel(x1 * 3 + 1, y1 * 3 + 1, j1);
							//	gameGraphics.drawMinimapPixel(x1 * 3, y1 * 3 + 2, j1);
							//}
						}
					}
				}

				// Roofs
				for (int x1 = 0; x1 < 95; x1++)
				{
					for (int y1 = 0; y1 < 95; y1++)
					{

						int wallType = getHorizontalWall(x1, y1);
						if (wallType > 0) setRoofTile(wallType - 1, x1, y1, x1 + 1, y1);
						wallType = getVerticalWall(x1, y1);
						if (wallType > 0) setRoofTile(wallType - 1, x1, y1, x1, y1 + 1);


						wallType = getDiagonalWall(x1, y1);
						if (wallType > 0 && wallType < 12000) setRoofTile(wallType - 1, x1, y1, x1 + 1, y1 + 1);
						if (wallType > 12000 && wallType < 24000) setRoofTile(wallType - 12001, x1 + 1, y1, x1, y1 + 1);

					}
				}

				for (int i5 = 1; i5 < 95; i5++)
				{
					for (int l6 = 1; l6 < 95; l6++)
					{
						int j9 = getTileRoofType(i5, l6);
						if (j9 > 0)
						{
							int l11 = i5;
							int i14 = l6;
							int j16 = i5 + 1;
							int k18 = l6;
							int j19 = i5 + 1;
							int j21 = l6 + 1;
							int l22 = i5;
							int j23 = l6 + 1;
							int l23 = 0;
							int j24 = roofTileHeight[l11][i14];
							int l24 = roofTileHeight[j16][k18];
							int j25 = roofTileHeight[j19][j21];
							int l25 = roofTileHeight[l22][j23];
							if (j24 > 0x13880)
								j24 -= 0x13880;
							if (l24 > 0x13880)
								l24 -= 0x13880;
							if (j25 > 0x13880)
								j25 -= 0x13880;
							if (l25 > 0x13880)
								l25 -= 0x13880;
							if (j24 > l23)
								l23 = j24;
							if (l24 > l23)
								l23 = l24;
							if (j25 > l23)
								l23 = j25;
							if (l25 > l23)
								l23 = l25;
							if (l23 >= 0x13880)
								l23 -= 0x13880;
							if (j24 < 0x13880)
								roofTileHeight[l11][i14] = l23;
							else
								roofTileHeight[l11][i14] -= 0x13880;
							if (l24 < 0x13880)
								roofTileHeight[j16][k18] = l23;
							else
								roofTileHeight[j16][k18] -= 0x13880;
							if (j25 < 0x13880)
								roofTileHeight[j19][j21] = l23;
							else
								roofTileHeight[j19][j21] -= 0x13880;
							if (l25 < 0x13880)
								roofTileHeight[l22][j23] = l23;
							else
								roofTileHeight[l22][j23] -= 0x13880;
						}
					}

				}

				Roof.Reset();



				for (int tileX = 1; tileX < 95; tileX++)
				{
					for (int tileY = 1; tileY < 95; tileY++)
					{
						int roofTypeIndex = getTileRoofType(tileX, tileY);
						if (roofTypeIndex > 0)
						{
							int x1 = tileX;
							int y1 = tileY;
							int x2 = tileX + 1;
							int y2 = tileY;
							int x3 = tileX + 1;
							int y3 = tileY + 1;
							int x4 = tileX;
							int y4 = tileY + 1;
							int x5 = tileX * 128;
							int y5 = tileY * 128;
							int x6 = x5 + 128;
							int y6 = y5 + 128;
							int x7 = x5;
							int y7 = y5;
							int x8 = x6;
							int y8 = y6;
							int roofTileLeft = roofTileHeight[x1][y1];
							int roofTileRight = roofTileHeight[x2][y2];
							int roofTileUpRight = roofTileHeight[x3][y3];
							int roofTileUpLeft = roofTileHeight[x4][y4];
							int roofValue = Data.roofs[roofTypeIndex - 1];
							if (isRoofTile(x1, y1) && roofTileLeft < 0x13880)
							{
								roofTileLeft += roofValue + 0x13880;
								roofTileHeight[x1][y1] = roofTileLeft;
							}
							if (isRoofTile(x2, y2) && roofTileRight < 0x13880)
							{
								roofTileRight += roofValue + 0x13880;
								roofTileHeight[x2][y2] = roofTileRight;
							}
							if (isRoofTile(x3, y3) && roofTileUpRight < 0x13880)
							{
								roofTileUpRight += roofValue + 0x13880;
								roofTileHeight[x3][y3] = roofTileUpRight;
							}
							if (isRoofTile(x4, y4) && roofTileUpLeft < 0x13880)
							{
								roofTileUpLeft += roofValue + 0x13880;
								roofTileHeight[x4][y4] = roofTileUpLeft;
							}
							if (roofTileLeft >= 0x13880)
								roofTileLeft -= 0x13880;
							if (roofTileRight >= 0x13880)
								roofTileRight -= 0x13880;
							if (roofTileUpRight >= 0x13880)
								roofTileUpRight -= 0x13880;
							if (roofTileUpLeft >= 0x13880)
								roofTileUpLeft -= 0x13880;
							byte byte0 = 16;
							if (!hasRoofTiles(x1 - 1, y1))
								x5 -= byte0;
							if (!hasRoofTiles(x1 + 1, y1))
								x5 += byte0;
							if (!hasRoofTiles(x1, y1 - 1))
								y5 -= byte0;
							if (!hasRoofTiles(x1, y1 + 1))
								y5 += byte0;
							if (!hasRoofTiles(x2 - 1, y2))
								x6 -= byte0;
							if (!hasRoofTiles(x2 + 1, y2))
								x6 += byte0;
							if (!hasRoofTiles(x2, y2 - 1))
								y7 -= byte0;
							if (!hasRoofTiles(x2, y2 + 1))
								y7 += byte0;
							if (!hasRoofTiles(x3 - 1, y3))
								x8 -= byte0;
							if (!hasRoofTiles(x3 + 1, y3))
								x8 += byte0;
							if (!hasRoofTiles(x3, y3 - 1))
								y6 -= byte0;
							if (!hasRoofTiles(x3, y3 + 1))
								y6 += byte0;
							if (!hasRoofTiles(x4 - 1, y4))
								x7 -= byte0;
							if (!hasRoofTiles(x4 + 1, y4))
								x7 += byte0;
							if (!hasRoofTiles(x4, y4 - 1))
								y8 -= byte0;
							if (!hasRoofTiles(x4, y4 + 1))
								y8 += byte0;
							roofTypeIndex = Data.aln[roofTypeIndex - 1];
							//roofTileLeft = -roofTileLeft;
							//roofTileRight = -roofTileRight;
							//roofTileUpRight = -roofTileUpRight;
							//roofTileUpLeft = -roofTileUpLeft;

							if (getDiagonalWall(tileX, tileY) > 12000 && getDiagonalWall(tileX, tileY) < 24000 && getTileRoofType(tileX - 1, tileY - 1) == 0)
							{
								int[] ai8 = new int[3];
								ai8[0] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
								ai8[1] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
								ai8[2] = Roof.getVertexIndex(x6, roofTileRight, y7);
								Roof.AddFace(3, ai8, roofTypeIndex, NULLINTEGER);
							}
							else if (getDiagonalWall(tileX, tileY) > 12000 && getDiagonalWall(tileX, tileY) < 24000 && getTileRoofType(tileX + 1, tileY + 1) == 0)
							{
								int[] ai9 = new int[3];
								ai9[0] = Roof.getVertexIndex(x5, roofTileLeft, y5);
								ai9[1] = Roof.getVertexIndex(x6, roofTileRight, y7);
								ai9[2] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
								Roof.AddFace(3, ai9, roofTypeIndex, NULLINTEGER);
							}
							else if (getDiagonalWall(tileX, tileY) > 0 && getDiagonalWall(tileX, tileY) < 12000 && getTileRoofType(tileX + 1, tileY - 1) == 0)
							{
								int[] ai10 = new int[3];
								ai10[0] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
								ai10[1] = Roof.getVertexIndex(x5, roofTileLeft, y5);
								ai10[2] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
								Roof.AddFace(3, ai10, roofTypeIndex, NULLINTEGER);
							}
							else if (getDiagonalWall(tileX, tileY) > 0 && getDiagonalWall(tileX, tileY) < 12000 && getTileRoofType(tileX - 1, tileY + 1) == 0)
							{
								var ai11 = new int[3];
								ai11[0] = Roof.getVertexIndex(x6, roofTileRight, y7);
								ai11[1] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
								ai11[2] = Roof.getVertexIndex(x5, roofTileLeft, y5);
								Roof.AddFace(3, ai11, roofTypeIndex, NULLINTEGER);
							}
							else
								if (roofTileLeft == roofTileRight && roofTileUpRight == roofTileUpLeft)
								{

									var ai12 = new int[4];
									ai12[0] = Roof.getVertexIndex(x5, roofTileLeft, y5);
									ai12[1] = Roof.getVertexIndex(x6, roofTileRight, y7);
									ai12[2] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
									ai12[3] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);

									var ai13 = new int[] { ai12[0], ai12[3], ai12[2] };
									Roof.AddFace(3, ai13, roofTypeIndex, NULLINTEGER);

									ai13 = new[] { ai12[0], ai12[2], ai12[1] };
									Roof.AddFace(3, ai13, roofTypeIndex, NULLINTEGER);
								}
								else if (roofTileLeft == roofTileUpLeft && roofTileRight == roofTileUpRight)
								{
									var ai12 = new int[4];
									ai12[0] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
									ai12[1] = Roof.getVertexIndex(x5, roofTileLeft, y5);
									ai12[2] = Roof.getVertexIndex(x6, roofTileRight, y7);
									ai12[3] = Roof.getVertexIndex(x8, roofTileUpRight, y6);

									// Roof.AddFace(4, ai13, i12, NULLINTEGER);

									var ai13 = new int[] { ai12[0], ai12[3], ai12[2] };
									Roof.AddFace(3, ai13, roofTypeIndex, NULLINTEGER);

									ai13 = new[] { ai12[0], ai12[2], ai12[1] };
									Roof.AddFace(3, ai13, roofTypeIndex, NULLINTEGER);
								}
								else
								{
									var flag = !(getTileRoofType(tileX - 1, tileY - 1) > 0);
									if (getTileRoofType(tileX + 1, tileY + 1) > 0)
										flag = false;
									if (!flag)
									{
										var ai14 = new int[3];
										ai14[0] = Roof.getVertexIndex(x6, roofTileRight, y7);
										ai14[1] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
										ai14[2] = Roof.getVertexIndex(x5, roofTileLeft, y5);
										Roof.AddFace(3, ai14, roofTypeIndex, NULLINTEGER);

										var ai16 = new int[3];
										ai16[0] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
										ai16[1] = Roof.getVertexIndex(x5, roofTileLeft, y5);
										ai16[2] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
										Roof.AddFace(3, ai16, roofTypeIndex, NULLINTEGER);
									}
									else
									{
										//			if (currentSectionObject == null)
										//				continue;

										var ai15 = new int[3];
										ai15[0] = Roof.getVertexIndex(x5, roofTileLeft, y5);
										ai15[1] = Roof.getVertexIndex(x6, roofTileRight, y7);
										ai15[2] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
										Roof.AddFace(3, ai15, roofTypeIndex, NULLINTEGER);

										var ai17 = new int[3];
										ai17[0] = Roof.getVertexIndex(x8, roofTileUpRight, y6);
										ai17[1] = Roof.getVertexIndex(x7, roofTileUpLeft, y8);
										ai17[2] = Roof.getVertexIndex(x6, roofTileRight, y7);
										Roof.AddFace(3, ai17, roofTypeIndex, NULLINTEGER);
									}
								}
						}
					}

				}


				//for (int j12 = 0; j12 < 96; j12++)
				//{
				//	for (int k14 = 0; k14 < 96; k14++)
				//		if (roofTileHeight[j12][k14] >= 0x13880)
				//			roofTileHeight[j12][k14] -= 0x13880;

				//}

				/* Additional stuff not added yet */

				Roof.InverseVertices();

				//	Wall.InverseVertices();

				Terrain.InverseVertices();
			}
		}

		public static bool isRoofTile(int x, int y)
		{
			return getTileRoofType(x, y) > 0 && getTileRoofType(x - 1, y) > 0 && getTileRoofType(x - 1, y - 1) > 0 && getTileRoofType(x, y - 1) > 0;
		}

		public static bool hasRoofTiles(int x, int y)
		{
			return getTileRoofType(x, y) > 0 || getTileRoofType(x - 1, y) > 0 || getTileRoofType(x - 1, y - 1) > 0 || getTileRoofType(x, y - 1) > 0;
		}



		public static void setRoofTile(int objType, int srcX, int srcY, int destX, int destY)
		{
			// 0x13880 = 80000 decimal
			// dont think theres any problem here. 
			// Data.wallObjectModelHeight is not the problem either, i debugged the java version and got the same values both here and there. :p
			int height = Data.wallObjectModelHeight[objType];
			if (roofTileHeight[srcX][srcY] < 0x13880)
				roofTileHeight[srcX][srcY] += 0x13880 + height;
			if (roofTileHeight[destX][destY] < 0x13880)
				roofTileHeight[destX][destY] += 0x13880 + height;
		}

		public static void setTileType(int x, int y, int type)
		{
			tiles[x][y] |= type;
		}

		public static void loadSection(int x, int y)
		{
			int gridSize = 48;

			loadSection(x * gridSize + 23, y * gridSize + 23, 0);

		}


		public static int getTileRoofType(int arg0, int arg1)
		{
			if (arg0 < 0 || arg0 >= 96 || arg1 < 0 || arg1 >= 96)
				return 0;
			byte byte0 = 0;
			if (arg0 >= 48 && arg1 < 48)
			{
				byte0 = 1;
				arg0 -= 48;
			}
			else if (arg0 < 48 && arg1 >= 48)
			{
				byte0 = 2;
				arg1 -= 48;
			}
			else if (arg0 >= 48 && arg1 >= 48)
			{
				byte0 = 3;
				arg0 -= 48;
				arg1 -= 48;
			}
			return tileRoofType[byte0][arg0 * 48 + arg1];
		}


		public static void makeWall(ModelData wallObj, int wallObjIndex, int x, int y, int destX, int destY)
		{
			Vector2 textureUpperLeft = new Vector2(0.0f, 0.0f);
			Vector2 textureUpperRight = new Vector2(1.0f, 0.0f);
			Vector2 textureLowerLeft = new Vector2(0.0f, 1.0f);
			Vector2 textureLowerRight = new Vector2(1.0f, 1.0f);

			if (TextureManager.TextureAtlas != null)
			{
				var coords = TextureManager.GetWallTextureFromAtlas(wallObjIndex);
				if (coords != null)
				{
					textureUpperLeft = new Vector2(coords.Rect.xMin, coords.Rect.yMin);
					textureUpperRight = new Vector2(coords.Rect.xMax, coords.Rect.yMin);
					textureLowerLeft = new Vector2(coords.Rect.xMin, coords.Rect.yMax);
					textureLowerRight = new Vector2(coords.Rect.xMax, coords.Rect.yMax);
				}
			}

			SetTileType(x, y, 40);
			SetTileType(destX, destY, 40);
			int height = Data.wallObjectModelHeight[wallObjIndex];
			int textureIndex = Data.wallObjectModel_TextureIndex[wallObjIndex];
			int colorIndex = Data.wallObjectModel_ColorIndex[wallObjIndex];
			int l2 = x * 128;
			int i3 = y * 128;
			int j3 = destX * 128;
			int k3 = destY * 128;

			int left = wallObj.getVertexIndex(l2, roofTileHeight[x][y], i3, true); // -roofTileHeight[x][y]
			int upLeft = wallObj.getVertexIndex(l2, roofTileHeight[x][y] + height, i3, true); // -roofTileHeight[x][y] - height
			int upRight = wallObj.getVertexIndex(j3, roofTileHeight[destX][destY] + height, k3, true); // -roofTileHeight[destX][destY] - height
			int right = wallObj.getVertexIndex(j3, roofTileHeight[destX][destY], k3, true); // -roofTileHeight[destX][destY]


			// Frontface
			{
				int l4 = wallObj.AddFace(3, new[] { left, upLeft, right }, textureIndex, colorIndex, wallObjIndex);
				wallObj.SetVertexUVByFaceIndex(l4, new[] { textureLowerLeft, textureUpperLeft, textureLowerRight }, false);
				wallObj.entityType[l4] = Data.wallObjectUnknown[wallObjIndex] == 5 ? EntityIndex_Wall + wallObjIndex : 0;
				wallObj.WallObjectIndex[l4] = wallObjIndex;

				if (colorIndex == NULLINTEGER)
					wallObj.SetFaceVisible(l4, false);

				var l5 = wallObj.AddFace(3, new[] { right, upLeft, upRight }, textureIndex, colorIndex, wallObjIndex);
				wallObj.SetVertexUVByFaceIndex(l5, new[] { textureLowerRight, textureUpperLeft, textureUpperRight }, false);
				wallObj.entityType[l5] = Data.wallObjectUnknown[wallObjIndex] == 5 ? EntityIndex_Wall + wallObjIndex : 0;
				wallObj.WallObjectIndex[l5] = wallObjIndex;


				if (colorIndex == NULLINTEGER)
					wallObj.SetFaceVisible(l5, false);
			}


			left = wallObj.getVertexIndex(l2, roofTileHeight[x][y], i3, true); // -roofTileHeight[x][y]
			upLeft = wallObj.getVertexIndex(l2, roofTileHeight[x][y] + height, i3, true); // -roofTileHeight[x][y] - height
			upRight = wallObj.getVertexIndex(j3, roofTileHeight[destX][destY] + height, k3, true); // -roofTileHeight[destX][destY] - height
			right = wallObj.getVertexIndex(j3, roofTileHeight[destX][destY], k3, true); // -roofTileHeight[destX][destY]


			// Backface
			{
				int l4 = wallObj.AddFace(3, new[] { left, upLeft, right }.Reverse().ToArray(), textureIndex, colorIndex, wallObjIndex);
				wallObj.SetVertexUVByFaceIndex(l4, new[] { textureLowerLeft, textureUpperLeft, textureLowerRight }.Reverse().ToArray(), false);
				wallObj.entityType[l4] = Data.wallObjectUnknown[wallObjIndex] == 5 ? EntityIndex_Wall + wallObjIndex : 0;
				wallObj.WallObjectIndex[l4] = wallObjIndex;

				if (colorIndex == NULLINTEGER)
					wallObj.SetFaceVisible(l4, false);

				var l5 = wallObj.AddFace(3, new[] { right, upLeft, upRight }.Reverse().ToArray(), textureIndex, colorIndex, wallObjIndex);
				wallObj.SetVertexUVByFaceIndex(l5, new[] { textureLowerRight, textureUpperLeft, textureUpperRight }.Reverse().ToArray(), false);
				wallObj.entityType[l5] = Data.wallObjectUnknown[wallObjIndex] == 5 ? EntityIndex_Wall + wallObjIndex : 0;
				wallObj.WallObjectIndex[l5] = wallObjIndex;

				if (colorIndex == NULLINTEGER)
					wallObj.SetFaceVisible(l5, false);
			}

		}



		public static void loadSection(int x, int y, int height)
		{
			cleanUpWorld();
			int sectionX = (x + 24) / 48;
			int sectionY = (y + 24) / 48;
			loadSection(x, y, height, true);
			if (height == 0)
			{
				loadSection(x, y, 1, false);
				loadSection(x, y, 2, false);
				loadSection(sectionX - 1, sectionY - 1, height, 0);
				loadSection(sectionX, sectionY - 1, height, 1);
				loadSection(sectionX - 1, sectionY, height, 2);
				loadSection(sectionX, sectionY, height, 3);
				stitchAreaTileColors();
			}
			Mudclient.TerrainUpdateNecessary = true;
		}

		public static void loadSection(int sectionX, int sectionY, int height, int sector)
		{
			String filename = "m" + height + sectionX / 10 + sectionX % 10 + sectionY / 10 + sectionY % 10;
			try
			{

				if (landscapeFree != null)
				{
					sbyte[] data = DataOperations.loadData(filename + ".hei", 0, landscapeFree);




					if (data == null && landscapeMembers != null)
						data = DataOperations.loadData(filename + ".hei", 0, landscapeMembers);

					var datalen = 0;
					if (data != null)
						datalen = data.Length;

					//	UnityEngine.Debug.Log(filename + ".hei - Loaded with: " + datalen + " bytes");


					if (data != null && data.Length > 0)
					{

						// UnityEngine.Debug.Log("Load Landscape " + sectionX + "," + sectionY + "," + height + "," + sector);
						int off = 0;
						int i2 = 0;
						for (int tile = 0; tile < 2304; )
						{
							int k3 = (data[off++] & 0xff);
							if (k3 < 128)
							{
								tileGroundElevation[sector][tile++] = (sbyte)k3;
								i2 = k3;
							}
							if (k3 >= 128)
							{
								for (int k4 = 0; k4 < k3 - 128; k4++)
									tileGroundElevation[sector][tile++] = (sbyte)i2;

							}
						}

						i2 = 64;
						for (int tile = 0; tile < 48; tile++)
						{
							for (int l4 = 0; l4 < 48; l4++)
							{
								i2 = (tileGroundElevation[sector][l4 * 48 + tile] + i2 & 0x7f);
								tileGroundElevation[sector][l4 * 48 + tile] = (sbyte)(i2 * 2);
							}

						}

						i2 = 0;
						for (int tile = 0; tile < 2304; )
						{
							int l5 = (data[off++] & 0xff);
							if (l5 < 128)
							{
								tileGroundTexture[sector][tile++] = l5;
								i2 = l5;
							}
							if (l5 >= 128)
							{
								for (int i7 = 0; i7 < l5 - 128; i7++)
									tileGroundTexture[sector][tile++] = i2;

							}
						}

						i2 = 35;
						for (int i6 = 0; i6 < 48; i6++)
						{
							for (int j7 = 0; j7 < 48; j7++)
							{
								i2 = (tileGroundTexture[sector][j7 * 48 + i6] + i2 & 0x7f);
								tileGroundTexture[sector][j7 * 48 + i6] = (i2 * 2);
							}

						}

					}
					else
					{
						for (int tile = 0; tile < 2304; tile++)
						{
							tileGroundElevation[sector][tile] = 0;
							tileGroundTexture[sector][tile] = 0;
						}

					}
					data = DataOperations.loadData(filename + ".dat", 0, mapsFree);
					if (data == null && mapsMembers != null)
						data = DataOperations.loadData(filename + ".dat", 0, mapsMembers);
					if (data == null || data.Length == 0)
						return;//throw new IOException();
					int off2 = 0;

					//#warning added & 0xff on marked, not original
					for (int tile = 0; tile < 2304; tile++)
						tileVerticalWall[sector][tile] = data[off2++]; // MARKED, should not have & 0xff

					for (int tile = 0; tile < 2304; tile++)
						tileHorizontalWall[sector][tile] = data[off2++]; // MARKED, should not have & 0xff

					for (int tile = 0; tile < 2304; tile++)
						tileDiagonalWall[sector][tile] = data[off2++] & 0xff;

					for (int tile = 0; tile < 2304; tile++)
					{
						int j6 = data[off2++] & 0xff;
						if (j6 > 0)
							tileDiagonalWall[sector][tile] = j6 + 12000;
					}

					for (int tile = 0; tile < 2304; )
					{
						int k7 = (data[off2++] & 0xff);
						if (k7 < 128)
						{
							tileRoofType[sector][tile++] = k7; //(sbyte)k7;
							//tileRoofType[sector][tile++] = 0;
						}
						else
						{
							for (int j8 = 0; j8 < k7 - 128; j8++)
								tileRoofType[sector][tile++] = 0;

						}
					}

					// Adds water on lower heights
					int l7 = 0;
					for (int tile = 0; tile < 2304; )
					{
						int i9 = (data[off2++] & 0xff);
						if (i9 < 128)
						{
							tileGroundOverlay[sector][tile++] = (sbyte)i9;
							l7 = i9;
						}
						else
						{
							for (int l9 = 0; l9 < i9 - 128; l9++)
								tileGroundOverlay[sector][tile++] = (sbyte)l7;

						}
					}

					for (int j9 = 0; j9 < 2304; )
					{
						int i10 = data[off2++] & 0xff;
						if (i10 < 128)
						{
							tileObjectRotation[sector][j9++] = (sbyte)i10;
						}
						else
						{
							for (int l10 = 0; l10 < i10 - 128; l10++)
								tileObjectRotation[sector][j9++] = 0;

						}
					}

					data = DataOperations.loadData(filename + ".loc", 0, mapsFree);
					if (data != null && data.Length > 0)
					{
						int k1 = 0;
						for (int j10 = 0; j10 < 2304; )
						{

							int i11 = data[k1++] & 0xff;
							if (i11 < 128)
								tileDiagonalWall[sector][j10++] = i11 + 48000;
							else
								j10 += i11 - 128;
						}

						return;
					}
				}
				else
				{

					// 	UnityEngine.Debug.Log("Load Landscape " + filename + ".jm");

					sbyte[] abyte1 = new sbyte[20736];
					DataOperations.readFully("../gamedata/maps/" + filename + ".jm", abyte1, 20736);
					int l1 = 0;
					int k2 = 0;
					for (int j3 = 0; j3 < 2304; j3++)
					{
						l1 = l1 + abyte1[k2++] & 0xff;
						tileGroundElevation[sector][j3] = (sbyte)l1;
					}

					l1 = 0;
					for (int j4 = 0; j4 < 2304; j4++)
					{
						l1 = l1 + abyte1[k2++] & 0xff;
						tileGroundTexture[sector][j4] = l1;
					}

					for (int k5 = 0; k5 < 2304; k5++)
						tileVerticalWall[sector][k5] = abyte1[k2++];

					for (int l6 = 0; l6 < 2304; l6++)
						tileHorizontalWall[sector][l6] = abyte1[k2++];

					for (int i8 = 0; i8 < 2304; i8++)
					{
						tileDiagonalWall[sector][i8] = (abyte1[k2] & 0xff) * 256 + (abyte1[k2 + 1] & 0xff);
						k2 += 2;
					}

					for (int l8 = 0; l8 < 2304; l8++)
						tileRoofType[sector][l8] = (abyte1[k2++]);

					for (int k9 = 0; k9 < 2304; k9++)
						tileGroundOverlay[sector][k9] = (abyte1[k2++]);

					for (int k10 = 0; k10 < 2304; k10++)
						tileObjectRotation[sector][k10] = (abyte1[k2++]);

				}
				return;
			}
			catch (IOException _ex)
			{
			}
			for (int k = 0; k < 2304; k++)
			{
				tileGroundElevation[sector][k] = 0;
				tileGroundTexture[sector][k] = 0;
				tileVerticalWall[sector][k] = 0;
				tileHorizontalWall[sector][k] = 0;
				tileDiagonalWall[sector][k] = 0;
				tileRoofType[sector][k] = 0;
				tileGroundOverlay[sector][k] = 0;
				if (height == 0)
					tileGroundOverlay[sector][k] = -6;
				if (height == 3)
					tileGroundOverlay[sector][k] = 8;
				tileObjectRotation[sector][k] = 0;
			}
		}

		public static int[][] tileHorizontalWall { get; set; }

		public static int[][] tileDiagonalWall { get; set; }

		public static int[][] tileGroundOverlay { get; set; }

		public static int[][] tileObjectRotation { get; set; }

		public static int[][] tileGroundTexture { get; set; }

		public static int[][] tileVerticalWall { get; set; }

		public static sbyte[][] tileGroundElevation { get; set; }

		public static int[][] tileRoofType { get; set; }

		public static GameObject[][] wallObject { get; set; }

		public static GameObject[][] roofObject { get; set; }

		public static int[][] roofTileHeight { get; set; }

		public static int[][] tiles { get; set; }

		public static int[][] steps { get; set; }

		public static int[][] objectDirs { get; set; }

		public static int[] selectedY { get; set; }

		public static int[] groundTexture { get; set; }

		public static GameObject[] TileChunks { get; set; }

		public static bool playerIsAlive { get; set; }

		public static int[] selectedX { get; set; }

		public static Color[] GroundTextureColor { get; set; }
	}
}
