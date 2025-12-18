using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Xml.Linq;

namespace buoi5.Models
{
    public partial class Khachhang
    {
        public Khachhang()
        {
            Phieudathang = new HashSet<Phieudathang>();
        }
        [Required(ErrorMessage = "Xin nhập mã khách hàng ")]// rang buoc nhap dl
        [Display(Name = "Mã khách hàng")]
        public string Makh { get; set; }
        [Required(ErrorMessage = "Xin nhập tên khách hàng ")]// rang buoc nhap dl
        [Display(Name = "Tên khách hàng")]
        public string Tenkh { get; set; }
        [Required(ErrorMessage = "Xin nhập năm sinh ")]// rang buoc nhap dl
        [Display(Name = "Năm sinh")]
        public int? Namsinh { get; set; }
       
        public bool? Phai { get; set; }
        [Required(ErrorMessage = "Xin nhập số điện thoại ")]// rang buoc nhap dl
        [Display(Name = "Số điện thoại")]
        public string Dienthoai { get; set; }
        [Required(ErrorMessage = "Xin nhập địa chỉ ")]// rang buoc nhap dl
        [Display(Name = "Địa chỉ")]
        public string Diachi { get; set; }
        [Required(ErrorMessage = "Xin nhập password ")]// rang buoc nhap dl
        [Display(Name = "Password")]
        public string Password { get; set; }

        public ICollection<Phieudathang> Phieudathang { get; set; }
    }
}
