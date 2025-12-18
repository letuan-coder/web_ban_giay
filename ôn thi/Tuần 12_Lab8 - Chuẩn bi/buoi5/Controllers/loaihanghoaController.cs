using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System;
using buoi5.Models;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
namespace buoi5.Controllers
{
    public class loaihanghoaController : Controller
    {
      
            QLBHContext db = new QLBHContext();
            public IActionResult Index()
            {
                ViewBag.lhh = db.Loaihanghoa;
                return View();
            }
            [HttpGet]
            public IActionResult formthemLHH()
            {
                return View();
            }
        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult themLHH([Bind(include: "Maloai,Tenloai")] Models.Loaihanghoa loaihanghoa)
        {
            Loaihanghoa n = db.Loaihanghoa.Find(loaihanghoa.Maloai);
            if (n == null && ModelState.IsValid)
            {
                ViewBag.n = null;
                db.Loaihanghoa.Add(loaihanghoa);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            else if (n != null)
            {
                ViewBag.nv = n;
                return View("loiThemLHH", loaihanghoa);
            }
            else
            {
                ViewBag.n = null;
                return View("formthemLHH");
            }
        }
        [HttpGet]
        public ActionResult loiThemLHH(Models.Loaihanghoa loaihanghoa)
        {
            return View("formthemLHH", loaihanghoa);
        }

        public ActionResult formxoaLHH(string id)
        {
            int dem = db.Hanghoa.Where(a => a.Maloai == id).ToList().Count; ///loc ra Msmh trung voi id
            ViewBag.flag = dem;
            Models.Loaihanghoa x = db.Loaihanghoa.Find(id);
            return View(x);

        }

        public ActionResult xoaLHH(string id)
        {
            Models.Loaihanghoa x = db.Loaihanghoa.Find(id);
            if (x != null)
            {
                db.Loaihanghoa.Remove(x);
                db.SaveChanges();
            }
            return RedirectToAction("Index"); /// goi ham index de view lai
        }

        public IActionResult formsuaLHH(string id)
        {
            Models.Loaihanghoa lhh = db.Loaihanghoa.Find(id);
            Models.Loaihanghoa x = new Models.Loaihanghoa
            {
                Maloai = lhh.Maloai,
                Tenloai = lhh.Tenloai,
            };
            return View(x);
        }
        public IActionResult suaLHH(Models.Loaihanghoa lhh)
        {
            if (ModelState.IsValid) //ktra gui dc hay ko dc tra ve gia tri true fales
            {
                Models.Loaihanghoa x = db.Loaihanghoa.Find(lhh.Maloai);
                if (x != null)
                {
                    x.Tenloai = lhh.Tenloai;
                    
                    db.SaveChanges();
                }
                return RedirectToAction("Index", "loaihanghoa");
            }
            else
            {
                return View("formsuaLHH");

            }
        }










    }
    }