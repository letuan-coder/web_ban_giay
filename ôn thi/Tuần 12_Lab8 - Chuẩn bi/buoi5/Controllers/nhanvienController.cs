using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System;
using buoi5.Models;
using Microsoft.EntityFrameworkCore.Metadata.Internal;

namespace buoi5.Controllers
{
    public class nhanvienController : Controller
    {
       
            private Models.QLBHContext db = new Models.QLBHContext();
            public IActionResult Index()
            {
                return View(db.Nhanvien.ToList());
            }
            public ActionResult formthemNV()
            {
                return View();
            }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult themNV([Bind(include: "Manv,Tennv,Ngaysinh,Phai,Diachi,Password")] Models.Nhanvien nhanvien)
        {
            Nhanvien n = db.Nhanvien.Find(nhanvien.Manv);
            if (n == null && ModelState.IsValid)
            {
                ViewBag.n = null;
                db.Nhanvien.Add(nhanvien);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            else if (n != null)
            {
                ViewBag.nv = n;
                return View("loiThemNV", nhanvien);

            }
            else
            {
                ViewBag.n = null;
                return View("formthemNV");
            }
        }
        [HttpGet]
        public ActionResult loiThemNV(Models.Nhanvien nhanvien)
        {       
                return View("formthemNV", nhanvien);
        }
        public ActionResult formxoaNV(string id)
            {
                int dem = db.Phieugiaohang.Where(a => a.Manv == id).ToList().Count; ///loc ra Msmh trung voi id
                ViewBag.flag = dem;
                Models.Nhanvien x = db.Nhanvien.Find(id);
                return View(x);

            }

            public ActionResult xoaNV(string id)
            {
                Models.Nhanvien x = db.Nhanvien.Find(id);
                if (x != null)
                {
                    db.Nhanvien.Remove(x);
                    db.SaveChanges();
                }
                return RedirectToAction("Index"); /// goi ham index de view lai
            }
        public IActionResult formsuaNV(string id)
        {
            Models.Nhanvien mh = db.Nhanvien.Find(id);
            Models.Nhanvien x = new Models.Nhanvien
            {
                Manv = mh.Manv,
                Tennv = mh.Tennv,
                Ngaysinh = mh.Ngaysinh,
                Phai = mh.Phai,
                Diachi = mh.Diachi,
                Password = mh.Password

            };
            return View(x);
        }

        public IActionResult suaNV(Models.Nhanvien mh)
        {
            if (ModelState.IsValid) //ktra gui dc hay ko dc tra ve gia tri true fales
            {
                Models.Nhanvien x = db.Nhanvien.Find(mh.Manv);
                if (x != null)
                {
                    x.Tennv = mh.Tennv;
                    x.Ngaysinh = mh.Ngaysinh;
                    x.Phai = mh.Phai;
                    x.Diachi = mh.Diachi;
                    x.Password= mh.Password;
                    db.SaveChanges();
                }
                return RedirectToAction("Index","nhanvien");
            }
            else
            {
                return View("formsuaNV");

            }
        }
    }


    }

