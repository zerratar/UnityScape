using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets
{
	using Assets.RSC;
	using Assets.RSC.IO;
	using Assets.RSC.Models;

	using UnityEngine;

	using Camera = UnityEngine.Camera;
	using GameObject = UnityEngine.GameObject;

	public class DebugScript : MonoBehaviour
	{
		public GameState state;
		public MeshFilter wallFilter;
		public void Start()
		{
			state = GameObject.Find("GameManager").GetComponent<GameState>();


		}

		public bool ObjectHit = false;
		public string ObjectHitName = "";
		public void Update()
		{
			//var pos = Input.mousePosition;
			Vector3 pos = new Vector3(Screen.width / 2 - 8, Screen.height / 2);
			var ray = Camera.main.ScreenPointToRay(pos);

			var hits = Physics.RaycastAll(ray, 1000f);

			ObjectHit = hits.Length > 0;
			if (ObjectHit)
			{
				var closest = hits.OrderBy(o => o.distance).FirstOrDefault();

				ObjectHitName = closest.collider.name;

				if (!wallFilter && state.gameDataLoaded && state.Wall)
				{
					wallFilter = state.Wall.GetComponent<MeshFilter>();
				}

				if (state && state.Wall && closest.collider.name.ToLower().Contains("wall") && wallFilter)
				{

					triangleIndex = closest.triangleIndex * 3;
					Mesh mesh = wallFilter.sharedMesh;
					Vector3[] vertices = mesh.vertices;
					int[] triangles = mesh.triangles;

					Vector3 p0 = vertices[triangles[closest.triangleIndex * 3 + 0]];
					Vector3 p1 = vertices[triangles[closest.triangleIndex * 3 + 1]];
					Vector3 p2 = vertices[triangles[closest.triangleIndex * 3 + 2]];


					Transform hitTransform = closest.collider.transform;

					var faceIndex = (closest.triangleIndex * 3);
					if (faceIndex < Engine.Wall.Faces.Count)
					{

						//	var wallface = Engine.Wall.Faces[faceIndex];

						//	var type = Engine.Wall.entityType[faceIndex];

						//	Debug.Log("tri: " + faceIndex + " ti: " + wallface.TextureIndex + ", fc: " + wallface.FaceColor + " type:" + type);


						p0 = hitTransform.TransformPoint(p0);
						p1 = hitTransform.TransformPoint(p1);
						p2 = hitTransform.TransformPoint(p2);
						Debug.DrawLine(p0, p1, Color.red, 0.1f);
						Debug.DrawLine(p1, p2, Color.red, 0.1f);
						Debug.DrawLine(p2, p0, Color.red, 0.1f);

						targetWallPos = 1f / 3f * (p0 + p1 + p2);// 1/3 * [ (0 + 0.2 + 0), (5.8 + 6.2 + 6.2), (-3 -3 -2.7) ] = 1/3 [ 0.2, 18.2, 8.7 ] 


					}
				}

			}
		}

		int triangleIndex = 0;
		Vector3 targetWallPos = Vector3.zero;

		public void OnGUI()
		{
			if (targetWallPos != Vector3.zero)
			{
				if (state && state.Wall && wallFilter)
				{

					var faceIndex = triangleIndex;

					if (faceIndex < Engine.Wall.Faces.Count)
					{
						Mesh mesh = wallFilter.sharedMesh;
						Vector3[] vertices = mesh.vertices;
						int[] triangles = mesh.triangles;

						var face = Engine.Wall.Faces[faceIndex];
						
						var wallTypeIndex = face.ObjectIndex;
						int textureIndex = Data.wallObjectModel_TextureIndex[wallTypeIndex];
						int colorIndex = Data.wallObjectModel_ColorIndex[wallTypeIndex];
						var wallObjectType = Data.wallObjectType[wallTypeIndex];
						var r = Camera.main.WorldToScreenPoint(targetWallPos);
						GUI.Label(new Rect(r.x, Screen.height - r.y, 200f, 200f), "Tri: " + triangleIndex + ", Wall: " + wallObjectType + " Type: " + wallTypeIndex + ", Tex: " + textureIndex + ", Clr: " + colorIndex);

					}


				}

			}
		}
	}
}
