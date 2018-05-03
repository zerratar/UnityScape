using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets.RSC.Models
{
	using Assets.RSC.Managers;

//	using UnityEditor.Sprites;

	using UnityEngine;

	public static class Compare
	{
		public static IEnumerable<T> DistinctBy<T, TIdentity>(this IEnumerable<T> source, Func<T, TIdentity> identitySelector)
		{
			return source.Distinct(Compare.By(identitySelector));
		}

		public static IEqualityComparer<TSource> By<TSource, TIdentity>(Func<TSource, TIdentity> identitySelector)
		{
			return new DelegateComparer<TSource, TIdentity>(identitySelector);
		}

		private class DelegateComparer<T, TIdentity> : IEqualityComparer<T>
		{
			private readonly Func<T, TIdentity> identitySelector;

			public DelegateComparer(Func<T, TIdentity> identitySelector)
			{
				this.identitySelector = identitySelector;
			}

			public bool Equals(T x, T y)
			{
				return Equals(identitySelector(x), identitySelector(y));
			}

			public int GetHashCode(T obj)
			{
				return identitySelector(obj).GetHashCode();
			}
		}
	}

	public class Vertex
	{
		public Vector3 Position
		{
			get;
			set;
		}
		public Color Color { get; set; }
		public int ColorIndex { get; set; }
		public Vertex(float x, float y, float z)
		{
			this.Visible = true;
			this.Position = new Vector3(x, y, z);
		}
		public Vector2 UV { get; set; }

		public bool UVSet { get; set; }

		public bool Visible { get; set; }
	}

	public class Face
	{
		public int VertexCount { get; set; }
		public List<int> Indices { get; set; }
		public int FaceColor { get; set; }
		public int TextureIndex { get; set; }

		public Color Color { get; set; }

		public bool Visible { get; set; }

		public Face()
		{
			Visible = true;
			Indices = new List<int>();
		}

		public Texture2D Texture { get; set; }

		public int ObjectIndex { get; set; }
	}

	public class TextureHolder
	{
		public Texture2D Texture { get; set; }
		public float Width { get; set; }
		public float Height { get; set; }
		public Point Location { get; set; }
	}



	public class AtlasRect
	{
		public Rect Rect { get; set; }
		public int TextureIndex { get; set; }
	}

	public class ModelData
	{
		public int[] WallObjectIndex;

		public Mesh Mesh;

		public List<Vertex> Vertices;

		public List<Face> Faces;

		public List<Vector2> Normal;

		public List<Vector2> UV;

		public int[] entityType;

		public int ObjectState = 0;

		public MeshTopology Topology;

		public ModelData(string name, MeshTopology meshTopology = MeshTopology.Triangles)
		{
			this.MeshName = name;
			this.Topology = meshTopology;

			Vertices = new List<Vertex>();

			Faces = new List<Face>();
			Normal = new List<Vector2>();
			UV = new List<Vector2>();

			WallObjectIndex = new int[18432 * 2];
			entityType = new int[18432 * 2]; // FACE COUNT
		}


		internal void Reset()
		{
			Vertices.Clear();
			Faces.Clear();
			Normal.Clear();
			UV.Clear();
		}

		public Color GetColorByIndex(int color)
		{
			try
			{
				if (color != Engine.NULLINTEGER)
				{
					var clr = Engine.GroundTextureColor[color];

					//	Debug.Log(clr);
					return clr;
				}
			}
			catch
			{

			} return Color.white;
		}

		public string MeshName = "";

		public void GenerateMesh()
		{
			SetVertexColors();

			Mesh = new Mesh();
			Mesh.name = MeshName;
			// Mesh.vertexCount = Vertices.Count;
			List<int> triangles = new List<int>();

			//	var visibleVertices = Vertices.Where(v => v.Visible).ToArray();

			Mesh.vertices = Vertices.Select(v => v.Position).ToArray();
			Mesh.colors = Vertices.Select(v => v.Color).ToArray();
			Mesh.uv = Vertices.Select(v => v.UV).ToArray();

			//	Debug.Log(Mesh.colors.Length + " v" + Mesh.vertices.Length);


			//	Debug.Log("color not white! " + Mesh.colors.Count(c => c.r > 0.2f && c.r < 0.99f));


			foreach (var f in Faces)
			{
				if (f.Visible)
				{
					triangles.AddRange(f.Indices);
				}
			}

			var facesWithTextures = Faces.Count(f => f.Texture != null);
			//	Debug.Log("Faces with textures: " + facesWithTextures);

			Mesh.SetIndices(triangles.ToArray(), Topology, 0);

			Mesh.RecalculateNormals();


		}





		//public int[] CreateIndices()
		//{
		//	var outputIndices = new List<int>();
		//	var faces = ModelData.getFaces();

		//	var outputIndicesVertexColored = new List<int>();
		//	var outputIndicesVertexTextured = new Dictionary<int, List<int>>();

		//	foreach (var i in faces)
		//	{
		//		var indexes = i.GetPoints();
		//		var indexCount = i.GetIndices().Length;
		//		var textureId = i.GetTextureIndex();
		//		var isColored = textureId <= 0;

		//		if (indexCount == 3)
		//		{ //triangle face (1 triangle)
		//			outputIndices.AddRange(indexes);
		//			outputIndices.Reverse();
		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 4)
		//		{ //QuadEntity face (2 triangles)

		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];

		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(three);
		//			outputIndices.Add(three); outputIndices.Add(four); outputIndices.Add(one);
		//			outputIndices.Reverse();
		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 5)
		//		{ // penta (pentagon) face (3 triangles)

		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];
		//			int five = indexes[4];

		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(five);
		//			outputIndices.Add(two); outputIndices.Add(three); outputIndices.Add(four);
		//			outputIndices.Add(two); outputIndices.Add(four); outputIndices.Add(five);
		//			outputIndices.Reverse();
		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 6)
		//		{
		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];
		//			int five = indexes[4];
		//			int six = indexes[5];

		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(six);
		//			outputIndices.Add(two); outputIndices.Add(five); outputIndices.Add(six);
		//			outputIndices.Add(two); outputIndices.Add(three); outputIndices.Add(five);
		//			outputIndices.Add(three); outputIndices.Add(four); outputIndices.Add(five);
		//			outputIndices.Reverse();
		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 8)
		//		{
		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];
		//			int five = indexes[4];
		//			int six = indexes[5];
		//			int seven = indexes[6];
		//			int eight = indexes[7];

		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(three);
		//			outputIndices.Add(one); outputIndices.Add(three); outputIndices.Add(four);
		//			outputIndices.Add(one); outputIndices.Add(four); outputIndices.Add(eight);
		//			outputIndices.Add(eight); outputIndices.Add(four); outputIndices.Add(five);
		//			outputIndices.Add(eight); outputIndices.Add(five); outputIndices.Add(seven);
		//			outputIndices.Add(five); outputIndices.Add(six); outputIndices.Add(seven);
		//			outputIndices.Reverse();
		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 7)
		//		{
		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];
		//			int five = indexes[4];
		//			int six = indexes[5];
		//			int seven = indexes[6];


		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(three);
		//			outputIndices.Add(one); outputIndices.Add(three); outputIndices.Add(seven);
		//			outputIndices.Add(three); outputIndices.Add(four); outputIndices.Add(seven);
		//			outputIndices.Add(four); outputIndices.Add(six); outputIndices.Add(seven);
		//			outputIndices.Add(four); outputIndices.Add(five); outputIndices.Add(six);
		//			outputIndices.Reverse();

		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 12)
		//		{
		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];
		//			int five = indexes[4];
		//			int six = indexes[5];
		//			int seven = indexes[6];
		//			int eight = indexes[7];
		//			int nine = indexes[8];
		//			int ten = indexes[9];
		//			int eleven = indexes[10];
		//			int twelve = indexes[11];

		//			outputIndices.Add(six); outputIndices.Add(seven); outputIndices.Add(eight);
		//			outputIndices.Add(eight); outputIndices.Add(nine); outputIndices.Add(six);
		//			outputIndices.Add(nine); outputIndices.Add(ten); outputIndices.Add(six);
		//			outputIndices.Add(ten); outputIndices.Add(eleven); outputIndices.Add(six);
		//			outputIndices.Add(eleven); outputIndices.Add(twelve); outputIndices.Add(six);
		//			outputIndices.Add(twelve); outputIndices.Add(one); outputIndices.Add(six);
		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(six);
		//			outputIndices.Add(two); outputIndices.Add(three); outputIndices.Add(six);
		//			outputIndices.Add(three); outputIndices.Add(four); outputIndices.Add(six);
		//			outputIndices.Add(four); outputIndices.Add(five); outputIndices.Add(six);

		//			outputIndices.Reverse();

		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else if (indexCount == 9)
		//		{
		//			int one = indexes[0];
		//			int two = indexes[1];
		//			int three = indexes[2];
		//			int four = indexes[3];
		//			int five = indexes[4];
		//			int six = indexes[5];
		//			int seven = indexes[6];
		//			int eight = indexes[7];
		//			int nine = indexes[8];

		//			outputIndices.Add(one); outputIndices.Add(two); outputIndices.Add(three);
		//			outputIndices.Add(one); outputIndices.Add(three); outputIndices.Add(four);
		//			outputIndices.Add(one); outputIndices.Add(four); outputIndices.Add(nine);
		//			outputIndices.Add(nine); outputIndices.Add(four); outputIndices.Add(five);
		//			outputIndices.Add(nine); outputIndices.Add(five); outputIndices.Add(eight);
		//			outputIndices.Add(eight); outputIndices.Add(six); outputIndices.Add(seven);

		//			outputIndices.Reverse();

		//			AddIndices(outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//		}
		//		else
		//		{

		//			// since cutting some of the ears fuck up. lets skip them.
		//			//	continue;



		//			// Ugly hack for cutting ears, creating indices this way if we are not able to do it any other way.
		//			// if (indexCount < 14)

		//			// outputIndices.AddRange(indexes);
		//			// outputIndices.AddRange(CutEars(indexes));

		//			if (indexes.Length % 3 == 0)
		//			{
		//				outputIndices.AddRange(indexes);

		//				outputIndices.Reverse();

		//				AddIndices(
		//					outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, indexCount);
		//			}
		//			else
		//			{
		//				int totalC = 0;
		//				for (int j = 0; j < indexes.Length; j += 3)
		//					if (j <= indexes.Length - 3)
		//						totalC += 3;

		//				for (int j = 0; j < totalC; j++)
		//					outputIndices.Add(indexes[j]);

		//				outputIndices.Reverse();

		//				AddIndices(
		//					outputIndices, ref outputIndicesVertexColored, ref outputIndicesVertexTextured, textureId, isColored, totalC);
		//			}
		//		}
		//	}


		// We are done, so lets create our meshes!

		// CreateModelMeshes(outputIndicesVertexColored, outputIndicesVertexTextured);

		// returns the total index buffer for this Ob3Model, used by our "legacy" code.
		// if something goes wrong, we will use this.
		//	return outputIndices.ToArray();
		//}


		public void SetFaceVisible(int faceIndex, bool visible)
		{
			var face = Faces[faceIndex];
			if (face != null)
			{
				face.Visible = visible;
				foreach (var i in face.Indices)
				{
					Vertices[i].Visible = visible;
				}
			}

		}

		internal int getVertexIndex(float x, float y, float z, bool forceCreateNew = false)
		{

			if (!forceCreateNew)
			{
				for (int j = 0; j < Vertices.Count; j++)
					if (Vertices[j].Position.x == x && Vertices[j].Position.y == y && Vertices[j].Position.z == z)
						return j;
			}
			//	if (vert_count >= totalVerticeCount) return -1;

			Vertices.Add(new Vertex(x, y, z));
			return Vertices.Count - 1;
		}

		internal void SetVertexColor(int vertexIndex, int color)
		{
			Vertices[vertexIndex].ColorIndex = color;
		}


		public int addVertex(int x, int y, int z)
		{
			// if (vert_count >= totalVerticeCount) return -1;
			Vertices.Add(new Vertex(x, y, z));
			return Vertices.Count - 1;

		}

		public static Color GetColor(int rscColor)
		{
			rscColor = -1 - rscColor;
			int k2 = (rscColor >> 10 & 0x1f) * 8;
			int j3 = (rscColor >> 5 & 0x1f) * 8;
			int l3 = (rscColor & 0x1f) * 8;
			return new Color(k2 / 255f, j3 / 255f, l3 / 255f, 1f);
		}

		public void ApplyWallTexture(int faceIndex, int wallIndex)
		{
			var face = Faces[faceIndex];
			if (face.TextureIndex != Engine.NULLINTEGER)
			{
				var t = TextureManager.GetWallTexture(wallIndex);
				if (t != null)
				{
					face.Texture = t;
				}
			}
		}

		public int AddFace(int vertexCount, int[] _faceVertices, int _faceBack, int _faceFront, int objIndex = -1)
		{
			// if (face_count >= totalFaceCount) return -1;

			var f = new Face();
			f.Color = GetColor(_faceFront);
			f.VertexCount = vertexCount;
			f.Indices = _faceVertices.ToList();
			f.TextureIndex = _faceBack;
			f.FaceColor = _faceFront;
			f.Visible = true;
			if (objIndex != -1)
				f.ObjectIndex = objIndex;
			Faces.Add(f);




			ObjectState = 1;

			return Faces.Count - 1;
		}

		public void SetVertexColors()
		{
			/*
			for (int i = 0; i < Vertices.Count; i++)
			{
				var matchingFace = Faces.FirstOrDefault(j => j.Indices.Any(p => p == i));
				if (matchingFace != null)
				{
					Vertices[i].Color = matchingFace.Color;
				}
			} */

			foreach (var f in Faces)
			{
				foreach (var i in f.Indices)
				{
					Vertices[i].Color = f.Color;
				}
			}
		}

		internal void InverseVertices()
		{
			foreach (var vert in this.Vertices)
			{
				vert.Position = new Vector3(vert.Position.x, vert.Position.y * -1f, vert.Position.z);
			}
		}

		internal void SetVertexUVByFaceIndex(int faceIndex, Vector2[] uv, bool overwrite)
		{

			var f = Faces[faceIndex];
			if (uv.Length != f.Indices.Count) return;


			for (int i = 0; i < f.Indices.Count; i++)
			{

				var i2 = f.Indices[i];

				if (Vertices[i2].UVSet && !overwrite) continue;

				Vertices[i2].UVSet = true;
				Vertices[i2].UV = uv[i];
			}
		}
	}
}
