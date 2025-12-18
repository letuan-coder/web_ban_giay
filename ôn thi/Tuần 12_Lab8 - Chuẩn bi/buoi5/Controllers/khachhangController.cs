using buoi5.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Newtonsoft.Json;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

namespace buoi5.Controllers
{
    public class khachhangController : Controller
    {

        private Models.QLBHContext db = new Models.QLBHContext();

        public IActionResult Index()
        {
            ViewBag.kh = db.Khachhang;
            return View();
        }
        public IActionResult DangNhap()
        {
            ViewBag.loginCheck = null;
            return View();
        }
        [HttpPost]
        public IActionResult DangNhap(Khachhang kh)
        {
            ViewBag.loginCheck = false;
            Khachhang k = db.Khachhang.Find(kh.Makh);
            if (k != null)
            {
                if (k.Password == kh.Password)
                {
                    string json = JsonConvert.SerializeObject(k);
                    HttpContext.Session.SetString("Khachhang", json);
                    ViewBag.loginCheck = true;
                    return RedirectToAction("Index", "Home");
                }
            }
            return View();
        }
        public IActionResult DangXuat()
        {
            HttpContext.Session.Remove("Khachhang");
            return RedirectToAction("Index", "Home");
        }
        public IActionResult sua(string id)
        {
            Khachhang kh = db.Khachhang.Find(id);
            if (kh == null)
                return NotFound();
            return View(kh);
        }
        [HttpPost]
        public IActionResult sua(Khachhang k)
        {
            if (ModelState.IsValid)
            {
                Khachhang kh = db.Khachhang.Find(k.Makh);
                if (kh == null)
                    return View("Index");
                else
                {
                    kh.Tenkh = k.Tenkh;
                    kh.Namsinh = k.Namsinh;
                    kh.Diachi = k.Diachi;
                    kh.Dienthoai = k.Dienthoai;
                    kh.Password = k.Password;
                    kh.Phai = k.Phai;
                    db.SaveChanges();
                    return RedirectToAction("Index", "Home");
                }
            }
            else
                return View();
        }
    }
}

