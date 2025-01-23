import React, { useEffect, useState, useContext } from 'react'
import Form from 'react-bootstrap/Form';
import { BiEdit } from "react-icons/bi";
import { AiOutlineDelete } from "react-icons/ai";
import { IoCreateOutline } from "react-icons/io5";
import { MdOutlineSettingsSuggest } from "react-icons/md";
import { HiRefresh } from "react-icons/hi";
import { TiInfoOutline } from "react-icons/ti";
import { Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import PageNation from '../PageNation/PageNation';
import { ToastContainer, toast } from "react-toastify";
import { SidebarContext } from '../ProtectedRouter/Layout';
axios.defaults.withCredentials = true

// testing for compare Company.js
//company management
const CompanyManagement = () => {

  const [companyData, setCompanyData] = useState([]);
  const [pageRecords, setPageRecords] = useState([]);
  const [jenkinsData, setJenkinesData] = useState([]);
  const [search, setSearch] = useState('');
  const searchKeys = ["name", "sapSubaccountName", "website", "subscriptions"]

  const [companyName, setCompanyName] = useState('')
  const [website, setWebsite] = useState('');
  const [sapSubaccountName, setSapSubAccount] = useState('');
  const [uuid, setUuid] = useState('');
  const [phone, setPhone] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [crosDomainTxt, setCrosDomainTxt] = useState();
  const [subscriptions, setSubscriptions] = useState();
  const [jenkinId, setJenkinId] = useState('');
  const [totalAdminLicenses, setTotalAdminLicenses] = useState('');
  const [totalUserLicenses, setTotalUserLicenses] = useState('');
  const [totalLiteUserLicenses, setTotalLiteUserLicenses] = useState('');
  const [volume, setVolume] = useState('');

  const [isEdit, setIsEdit] = useState(false);
  const [isDelete, setIsDelete] = useState(false);
  const [isAdd, setIsAdd] = useState(false);
  const [isTenantConfig, setIsTenantConfig] = useState(false);

  const currentDate = new Date().toISOString().split('T')[0];
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  const expireDate = date.toISOString().split('T')[0];

  const [isSapCloud, setIsSapCloud] = useState(false);
  const [cloudEnveronments, setCloudEnvironmensts] = useState()
  const [cloudStartDate, setCloudStartDate] = useState(currentDate);
  const [cloudExpiryDate, setCloudExpiryDate] = useState(expireDate);

  const [isHana, setIsHana] = useState(false);
  const [hanaEnveronments, setHanaEnvironmensts] = useState()
  const [hanaStartDate, setHanaStartDate] = useState(currentDate);
  const [hanaExpiryDate, setHanaExpiryDate] = useState(expireDate);

  const [isABAP, setIsABAP] = useState(false);
  const [abapEnveronments, setAbapEnvironmensts] = useState()
  const [abapStartDate, setAbapStartDate] = useState(currentDate);
  const [abapExpiryDate, setAbapExpiryDate] = useState(expireDate);

  const [isOnPremise, setIsOnPremise] = useState(false);
  const [onPremiseEnveronments, setOnPremiseEnvironmensts] = useState()
  const [onPremiseStartDate, setOnPremiseStartDate] = useState(currentDate);
  const [onPremiseExpiryDate, setOnPremiseExpiryDate] = useState(expireDate);

  const [isSapCpi, setIsSapCpi] = useState(false);
  const [sapCpiEnveronments, setSapCpiEnvironmensts] = useState()
  const [sapCpiStartDate, setSapCpiStartDate] = useState(currentDate);
  const [sapCpiExpiryDate, setSapCpiExpiryDate] = useState(expireDate);

  const [isSac, setIsSac] = useState(false);
  const [SacEnveronments, setSacEnvironmensts] = useState()
  const [SacStartDate, setSacStartDate] = useState(currentDate);
  const [SacExpiryDate, setSacExpiryDate] = useState(expireDate);

  const [isDatasphere, setIsDatasphere] = useState(false);
  const [DatasphereEnveronments, setDatasphereEnvironmensts] = useState()
  const [DatasphereStartDate, setDatasphereStartDate] = useState(currentDate);
  const [DatasphereExpiryDate, setDatasphereExpiryDate] = useState(expireDate);


  const [id, setId] = useState('');

  // Tenant Settings Configs
  const [secretKey, setSecretKey] = useState('');
  const [tdcTimeZone, setTdcTimeZone] = useState('');
  const [usComponentReq, setUsComponentReq] = useState(false);
  const [ignoreCVersForImport, setIgnoreCVersForImport] = useState(false);
  const [refreshTime, setRefreshTime] = useState();
  const [scheduler, setScheduler] = useState(false);
  const [apimgmt, setApimgmt] = useState(false)
  const [isAlias, setIsAlias] = useState(false);
  const [envData, setEnvData] = useState([])
  const [isPromotion, setIsPromotion] = useState(false);
  const [promotionData, setPromotionData] = useState({
    type: 'automatic',
    waittime: '',
    agile: false
  });

  const [isImpactanalysis, setIsImpactanalysis] = useState(false);
  const [isImportOneByOne, setIsImportOneByOne] = useState(false)
  const [roSapApiVersion, setRoSapApiVersion] = useState("latest");
  const [impactanalysis, setImpactanalysis] = useState({
    waitTime: '',
    noOfIterations: ''
  });


  const [sortedOrder, setSortedOrder] = useState("ASC");
  const navigate = useNavigate()

  let isOpenSideBar = useContext(SidebarContext);

  const SuccessToast = (data) => {
    toast.success(data, {
      position: "top-center",
      autoClose: 1000,
      theme: "light",
    });
  }

  const ErrorToast = (data) => {
    toast.error(data, {
      position: "top-center",
      autoClose: 1000,
      theme: "light",
    });
  }

  const CompanyDetails = () => {
    axios.get(`${process.env.REACT_APP_API_URL}/rateadmin/adminv2/companyManagement`)
      .then((res) => {
        const { data } = res;
        setCompanyData(data);
      }, (err) => {
        if (err.response.data.status === 403) {
          navigate("/")
        }
        else {
          ErrorToast("Internal server error")
        }
      }
      )
  }

  useEffect(() => {
    CompanyDetails();
    jenkins();
  },
    // eslint-disable-next-line
    []);

  const jenkins = () => {
    axios.get(`${process.env.REACT_APP_API_URL}/rateadmin/adminv2/ciservers`).then(
      (res) => {
        const { data } = res;
        setJenkinesData(data);
      }, (err) => {
        if (err.response.data.status === 403) {
          navigate("/")
        }
        else {
          ErrorToast("Internal server error")
        }
      }
    )
  }

  const RefreshAllCache = async () => {
    const apiUrls = [
      `${process.env.REACT_APP_API_URL}/saparate/generic/refreshcaches`,
      `${process.env.REACT_APP_API_URL}/rateadmin/api/admin/refreshcaches`,
      `${process.env.REACT_APP_API_URL}/rateloginserver/generic/refreshcaches`,
      `${process.env.REACT_APP_API_URL}/ratereports/generic/refreshcaches`,
      `${process.env.REACT_APP_API_URL}/ratesaptms/generic/refreshcaches`,
      `${process.env.REACT_APP_API_URL}/ratetestserver/generic/refreshcaches`,
      `${process.env.REACT_APP_API_URL}/rateworkflow/generic/refreshcaches`
    ];

    try {
      for (let i = 0; i < apiUrls.length; i++) {
        const res = await axios.post(apiUrls[i]);
        if (res.status !== 200) {
          ErrorToast(`Tenant Cache Refresh Failed with url ${apiUrls[i]}`);

        }
        if (i === apiUrls.length - 1) {
          SuccessToast(res.data);
        }
      }
    } catch (err) {
      if (err.response && err.response.status === 403) {
        navigate("/");
      } else {
        ErrorToast("Tenant Cache Refresh Failed.");
      }
    }
  };

  const Add = () => {
    ErraseFormData();
    setIsAdd(true);
    setIsEdit(true);
  }

  const FormData = () => {
    let modulesArray = [
      {
        type: "mtar",
        displayName: "SAP BTP (MTAR)",
        enabled: isSapCloud,
        environments: isSapCloud ? cloudEnveronments : 0,
        startDate: isSapCloud ? cloudStartDate : null,
        expiryDate: isSapCloud ? cloudExpiryDate : null
      },
      {
        type: "xsamtar",
        displayName: "SAP HANA XSA (MTAR)",
        enabled: isHana,
        environments: isHana ? hanaEnveronments : 0,
        startDate: isHana ? hanaStartDate : null,
        expiryDate: isHana ? hanaExpiryDate : null
      },
      {
        type: "abapcloud",
        displayName: "ABAP Cloud",
        enabled: isABAP,
        environments: isABAP ? abapEnveronments : 0,
        startDate: isABAP ? abapStartDate : null,
        expiryDate: isABAP ? abapExpiryDate : null
      },
      {
        type: "saponprem",
        displayName: "SAP On-Premise",
        enabled: isOnPremise,
        environments: isOnPremise ? onPremiseEnveronments : 0,
        startDate: isOnPremise ? onPremiseStartDate : null,
        expiryDate: isOnPremise ? onPremiseExpiryDate : null
      },
      {
        type: "sapcpi",
        displayName: "SAP CPI",
        enabled: isSapCpi,
        environments: isSapCpi ? sapCpiEnveronments : 0,
        startDate: isSapCpi ? sapCpiStartDate : null,
        expiryDate: isSapCpi ? sapCpiExpiryDate : null
      },
      {
        type: "sapsac",
        displayName: "SAP Analytics Cloud",
        enabled: isSac,
        environments: isSac ? SacEnveronments : 0,
        startDate: isSac ? SacStartDate : null,
        expiryDate: isSac ? SacExpiryDate : null
      },
      {
        type: "sapsds",
        displayName: "SAP Datasphere",
        enabled: isDatasphere,
        environments: isDatasphere ? DatasphereEnveronments : 0,
        startDate: isDatasphere ? DatasphereStartDate : null,
        expiryDate: isDatasphere ? DatasphereExpiryDate : null
      }
    ]

    modulesArray = modulesArray.filter((obj) => obj.enabled)

    return {
      id: id,
      name: companyName,
      website: website,
      sapSubaccountName: sapSubaccountName,
      orgUUID: uuid,
      corsdomain: Array.isArray(crosDomainTxt) ? crosDomainTxt : [crosDomainTxt],
      subscriptions: subscriptions,
      phone: phone,
      isActive: isActive,
      tenantSettings: {
        jenkinsId: jenkinId,
        totalAdminLicenses: totalAdminLicenses,
        totalUserLicenses: totalUserLicenses,
        totalLiteUsers: totalLiteUserLicenses,
        volumePath: volume,
        licenses: {
          modules: modulesArray
        }
      }
    }
  }

  const Validation = () => {
    const formdata = FormData();
    const licenses = formdata.tenantSettings.licenses.modules;
    isAdd === true ? licenses.length > 0 ? AddComapny() : ErrorToast("Please select licenses ")
      : licenses.length > 0 ? EditCompany() : ErrorToast("Please select licenses ")

  }

  const AddComapny = () => {
    const formdata = FormData()
    axios.post(`${process.env.REACT_APP_API_URL}/rateadmin/adminv2/companies`, formdata)
      .then((res) => {
        if (res.status === 200) {
          setIsEdit(false);
          SuccessToast(` Successfully saved '${companyName}'`)
          ErraseFormData()
          CompanyDetails();
        }
      }, (err) => {
        if (err.response.data.status === 403) {
          navigate("/")
        }
        else {
          setIsEdit(false);
          ErrorToast("Company data not saved")
          ErraseFormData()
        }
      })
  }

  const Edit = (data) => {
    ErraseFormData();
    setId(data.id);
    setCompanyName(data.name);
    setWebsite(data.website);
    setSapSubAccount(data.sapSubaccountName);
    setUuid(data.orgUUID);
    setPhone(data.phone);
    setIsActive(data.isActive);
    setCrosDomainTxt(data.corsdomain);
    setSubscriptions(data.subscriptions);
    setTotalAdminLicenses(data.tenantSettings.totalAdminLicenses);
    setTotalUserLicenses(data.tenantSettings.totalUserLicenses);
    setTotalLiteUserLicenses(data.tenantSettings.totalLiteUsers)
    setVolume(data.tenantSettings.volumePath);
    setJenkinId(data.tenantSettings.jenkinsId)

    const array = data.tenantSettings.licenses.modules;

    array.forEach((obj) => {
      switch (obj.type) {
        case "mtar": {
          setIsSapCloud(obj.enabled);
          if (obj.enabled) {
            setCloudEnvironmensts(obj.environments)
            setCloudStartDate(obj.startDate);
            setCloudExpiryDate(obj.expiryDate);
          }
          else {
            setCloudEnvironmensts()
            setCloudStartDate(currentDate)
            setCloudExpiryDate(expireDate)
          }
          break;
        }
        case "xsamtar": {
          setIsHana(obj.enabled);
          if (obj.enabled) {
            setHanaEnvironmensts(obj.environments);
            setHanaStartDate(obj.startDate);
            setHanaExpiryDate(obj.expiryDate);
          }
          else {
            setHanaEnvironmensts();
            setHanaStartDate(currentDate);
            setHanaExpiryDate(expireDate);
          }
          break;
        }
        case "abapcloud": {
          setIsABAP(obj.enabled);
          if (obj.enabled) {
            setAbapEnvironmensts(obj.environments);
            setAbapStartDate(obj.startDate);
            setAbapExpiryDate(obj.expiryDate);
          }
          else {
            setAbapEnvironmensts();
            setAbapStartDate(currentDate);
            setAbapExpiryDate(expireDate);
          }
          break;
        }
        case "saponprem": {
          setIsOnPremise(obj.enabled);
          if (obj.enabled) {
            setOnPremiseEnvironmensts(obj.environments);
            setOnPremiseStartDate(obj.startDate);
            setOnPremiseExpiryDate(obj.expiryDate);
          }
          else {
            setOnPremiseEnvironmensts();
            setOnPremiseStartDate(currentDate);
            setOnPremiseExpiryDate(expireDate);
          }
          break;
        }
        case "sapcpi": {
          setIsSapCpi(obj.enabled);
          if (obj.enabled) {
            setSapCpiEnvironmensts(obj.environments);
            setSapCpiStartDate(obj.startDate);
            setSapCpiExpiryDate(obj.expiryDate)
          }
          else {
            setSapCpiEnvironmensts();
            setSapCpiStartDate(currentDate);
            setSapCpiExpiryDate(expireDate);
          }
          break;
        }
        case "sapsac": {
          setIsSac(obj.enabled);
          if (obj.enabled) {
            setSacEnvironmensts(obj.environments);
            setSacStartDate(obj.startDate);
            setSacExpiryDate(obj.expiryDate);
          }
          else {
            setSacEnvironmensts();
            setSacStartDate(currentDate);
            setSacExpiryDate(expireDate);
          }
          break;
        }
        case "sapsds": {
          setIsDatasphere(obj.enabled);
          if (obj.enabled) {
            setDatasphereEnvironmensts(obj.environments);
            setDatasphereStartDate(obj.startDate);
            setDatasphereExpiryDate(obj.expiryDate);
          }
          else {
            setDatasphereEnvironmensts();
            setDatasphereStartDate(currentDate);
            setDatasphereExpiryDate(expireDate);
          }
          break;
        }
        default:
          console.error("License is not registered in ro-admin");
          break;
      }

    })

    setIsEdit(true);
    setIsAdd(false)
  }

  const TenantSettingConfig = (ele) => {
    ErraseTenantSettingConfig();
    setSapSubAccount(ele.sapSubaccountName);
    setId(ele.tenantSettings.sid);
    const config = ele.tenantSettings.config;
    if (config !== null) {
      setSecretKey(config.secretKey);
      setTdcTimeZone(config.tdcTimeZone);
      setUsComponentReq(config.usComponentReq);
      setIgnoreCVersForImport(config.ignoreCVersForImport);
      if (config.features !== null) {
        setScheduler(config.features.scheduler);
        setApimgmt(config.features.apimgmt)
      }
      if (config.cpi_alias !== null) {
        setIsAlias(true);
        setEnvData(config.cpi_alias.environments)
      }
      if (config.promotion != null) {
        setIsPromotion(true);
        setPromotionData(config.promotion);
      }
      if (config.trconfig != null) {
        if (config.trconfig.impactanalysis != null) {
          setIsImpactanalysis(true);
        }
        setIsImportOneByOne(config.trconfig.importOneByOne)
        setRoSapApiVersion(config.trconfig.roSapApiVersion)
        if (config.trconfig.impactanalysis != null) {
          setImpactanalysis(config.trconfig.impactanalysis);
        }
      }
    }
    setIsTenantConfig(true);
  }

  const addEnv = () => {
    setEnvData([...envData, { envId: '', aliasType: '', aliasStr: '', findStr: '' }])

  }

  const handleChange = (e, i) => {
    const { name, value } = e.target;
    const changedata = [...envData];
    changedata[i][name] = value;
    setEnvData(changedata);
  }

  const handleTrConfig = (e) => {
    const { name, value } = e.target;
    setImpactanalysis({ ...impactanalysis, [name]: value });
  }

  const handlePromotion = (e) => {
    const { name } = e.target;
    let value = e.target.value;
    if (name === 'agile') {
      value = !promotionData.agile;
    }
    setPromotionData({ ...promotionData, [name]: value });
  }

  const handleDelete = (i) => {
    const deleteEnv = [...envData]
    deleteEnv.splice(i, 1);
    setEnvData(deleteEnv);
  }

  const saveTenantSettingConfig = () => {
    const tr = {
      importOneByOne: isImportOneByOne,
      impactanalysis: isImpactanalysis ? impactanalysis : null,
      roSapApiVersion: roSapApiVersion
    };
    const Alias = {
      environments: envData
    };

    const tenantCofig = {
      features: {
        scheduler: scheduler,
        apimgmt: apimgmt
      },

      cpi_alias: isAlias ? Alias : null,
      promotion: isPromotion ? promotionData : null,
      trconfig: tr,
      secretKey: secretKey,
      tdcTimeZone: tdcTimeZone,
      usComponentReq: usComponentReq,
      ignoreCVersForImport: ignoreCVersForImport,
      bufferRefreshRateTime: refreshTime
    };
    saveTenantConfig(tenantCofig);
  }

  const saveTenantConfig = (tenantCofig) => {

    axios.post(`${process.env.REACT_APP_API_URL}/rateadmin/adminv2/tenantsettings/config/tenantname/${id}`, tenantCofig)
      .then((res) => {
        setIsTenantConfig(false);
        SuccessToast(` Successfully updated Tenant Setting Config'${sapSubaccountName}'`);
        ErraseTenantSettingConfig();
        CompanyDetails();
      }, (err) => {
        if (err.response.data.status === 403) {
          navigate("/")
        }
        else {
          ErrorToast(`Tenant Setting Config is not Updated for tenant name '${sapSubaccountName}'`)
          setIsTenantConfig(false);
          ErraseTenantSettingConfig()
        }
      })

  }

  const EditCompany = () => {
    const formdata = FormData();
    axios.post(`${process.env.REACT_APP_API_URL}/rateadmin/adminv2/companies/${formdata.id}`, formdata)
      .then((res) => {
        setIsEdit(false);
        SuccessToast(` Successfully updated '${companyName}'`);
        ErraseFormData();
        CompanyDetails();
      }, (err) => {
        if (err.response.data.status === 403) {
          navigate("/")
        }
        else {
          ErrorToast(` Company data not Updated '${companyName}'`)
          setIsEdit(false);
          ErraseFormData()
        }
      })
  }

  const Delete = (id, name) => {
    setId(id);
    setCompanyName(name);
    setIsDelete(true);
  }

  const DeleteCompany = () => {
    axios.delete(`${process.env.REACT_APP_API_URL}/rateadmin/adminv2/companies/${id}`)
      .then((res) => {
        if (res.status === 200) {
          SuccessToast("deleted Successfully")
          setIsDelete(!isDelete);
          CompanyDetails();
        }
      }, (err) => {
        if (err.response.data.status === 403) {
          navigate("/")
        }
        else {
          ErrorToast("Company data not Deleted")
          setIsDelete(!isDelete);
        }
      }
      )
    setIsDelete(false);
  }

  const ErraseFormData = () => {
    setId('');
    setCompanyName('');
    setWebsite('');
    setSapSubAccount('');
    setUuid('');
    setPhone('');
    setCrosDomainTxt('');
    setSubscriptions();
    setJenkinId();
    setTotalAdminLicenses();
    setTotalUserLicenses();
    setTotalLiteUserLicenses();
    setVolume('');
    setIsActive(true);

    setIsSapCloud(false);
    setCloudEnvironmensts();
    setCloudStartDate(currentDate);
    setCloudExpiryDate(expireDate);

    setIsHana(false);
    setHanaEnvironmensts();
    setHanaStartDate(currentDate);
    setHanaExpiryDate(expireDate);

    setIsABAP(false);
    setAbapEnvironmensts();
    setAbapStartDate(currentDate);
    setAbapExpiryDate(expireDate);

    setIsOnPremise(false);
    setOnPremiseEnvironmensts();
    setOnPremiseStartDate(currentDate);
    setOnPremiseExpiryDate(expireDate);

    setIsSapCpi(false);
    setSapCpiEnvironmensts();
    setSapCpiStartDate(currentDate);
    setSapCpiExpiryDate(expireDate)

    setIsSac(false);
    setSacEnvironmensts();
    setSacStartDate(currentDate);
    setSacExpiryDate(expireDate);

    setIsDatasphere(false);
    setDatasphereEnvironmensts();
    setDatasphereStartDate(currentDate);
    setDatasphereExpiryDate(expireDate)
  }

  const ErraseTenantSettingConfig = () => {
    setId('');
    setSapSubAccount('');
    setScheduler(false);
    setApimgmt(false)
    setSecretKey('');
    setTdcTimeZone('');
    setUsComponentReq('');
    setIgnoreCVersForImport('');
    setEnvData([])
    setPromotionData({
      type: 'automatic',
      waittime: '',
      agile: false
    });
    setIsImportOneByOne(false);
    setRoSapApiVersion("latest")
    setImpactanalysis({
      waitTime: '',
      noOfIterations: ''
    });
    setIsImpactanalysis(false);
    setIsPromotion(false);
    setIsAlias(false);
  }

  const Sorting = (col) => {
    if (sortedOrder === "ASC") {
      const sort = [...companyData]
        .sort((a, b) => {
          return (
            typeof a[col] === "string" && typeof a[col] === typeof b[col] ? a[col].toLowerCase() > b[col].toLowerCase() ? 1 : -1 : a[col] > b[col] ? 1 : -1
          )
        })
      setCompanyData(sort);
      setSortedOrder("DSC");
    }
    if (sortedOrder === "DSC") {
      const sort = [...companyData]
        .sort((a, b) => {
          return (
            typeof a[col] === "string" && typeof a[col] === typeof b[col] ? a[col].toLowerCase() < b[col].toLowerCase() ? 1 : -1 : a[col] < b[col] ? 1 : -1
          )
        })
      setCompanyData(sort)
      setSortedOrder("ASC")
    }
  }

  const searchData = () => {
    let data;
    if (search !== '' && companyData.length > 0) {
      data = companyData.filter((data) => searchKeys.some((searchKeys) => {
        return (
          typeof data[searchKeys] === "string" ? data[searchKeys].toLowerCase().includes(search.toLowerCase()) : `${data[searchKeys]}`.includes(search)
        )
      }))
    }
    else {
      data = companyData.slice(0, 10)
    }
    setPageRecords(data)
  }

  useEffect(() => {
    searchData();
  }, [search])

  return (
    <div className='company'>
      <div className='d-flex'>
        <div className='w-100 h-100' style={{ margin: "0 5px" }}>
          <div className='main-content' style={{ marginLeft: isOpenSideBar ? "15.5rem" : "3rem" }}>
            <div className='card cardStyle'>
              <div className='card-body'>
                <div className='header d-flex justify-content-between'>
                  <div className='fontStyle' style={{ padding: '0.55rem' }}>Companies</div>
                  <div className='fontStyle  d-flex text-primary p-1' >
                    <div className='d-flex create me-3' onClick={() => RefreshAllCache()}>
                      <HiRefresh className='fs-5 ' />
                      <div style={{ margin: "1px 0px 0px 5px" }}> Refresh Tenant Cache</div>
                    </div>
                    <div className='create d-flex' onClick={() => Add()}>
                      <IoCreateOutline className='fs-5 ' />
                      <div style={{ margin: "1px 0px 0px 5px" }}>
                        Create New Company
                      </div>
                    </div>
                  </div>
                </div>
                <hr />
                <div className='d-flex justify-content-end' >
                  <div className='d-flex align-items-center'>
                    <label className='fontStyle'>Search</label>
                    <input type='search'
                      className=' search-box form-control form-control-sm ms-2 '
                      value={search}
                      onChange={(e) => setSearch(e.target.value)} />
                  </div>
                </div>
                <hr />
                <div className='table-data table-responsive'>
                  <table className='table table-sm'>
                    <thead>
                      <tr className='table-headers'>
                        <th onClick={() => Sorting("name")}>Name</th>
                        <th onClick={() => Sorting("sapSubaccountName")} className='text-nowrap'>SAP Subaccount</th>
                        <th onClick={() => Sorting("website")}>Website</th>
                        <th onClick={() => Sorting("isActive")}>IsActive</th>
                        <th onClick={() => Sorting("subscriptions")}>Subscriptions</th>
                        <th onClick={() => Sorting("activatedDate")} className='text-nowrap'>Activated date</th>
                        <th onClick={() => Sorting("renewalDate")} className='text-nowrap'>Renewal date</th>
                        <th className='text-nowrap'>Admin Licenses</th>
                        <th className='text-nowrap'>User Licenses</th>
                        <th className='text-nowrap'>LiteUser Licenses</th>
                        <th colSpan={3}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {companyData.length > 0 && pageRecords.map((ele, index) => {
                        return (
                          <tr key={index} >
                            <td>{ele.name}</td>
                            <td>{ele.sapSubaccountName}</td>
                            <td>{ele.website}</td>
                            <td>{ele.isActive === true ? "Active" : "InActive"}</td>
                            <td>{ele.subscriptions}</td>
                            <td>{ele.activatedDate !== null ? new Date(ele.activatedDate).toDateString() : ''}</td>
                            <td>{ele.renewalDate !== null ? new Date(ele.renewalDate).toDateString() : ''}</td>
                            <td>{ele.tenantSettings !== null ? ele.tenantSettings.totalAdminLicenses : ''}</td>
                            <td>{ele.tenantSettings !== null ? ele.tenantSettings.totalUserLicenses : ''}</td>
                            <td>{ele.tenantSettings !== null ? ele.tenantSettings.totalLiteUserLicenses : ''}</td>
                            <td >
                              <BiEdit className=' actions mt-1' title='Edit' onClick={() => Edit(ele)} />
                            </td>
                            <td>
                              <MdOutlineSettingsSuggest className=' actions mt-1' title='Tenant Setting Config' onClick={() => TenantSettingConfig(ele)} />
                            </td>
                            <td>
                              <AiOutlineDelete className="actions mt-1" title='Delete' onClick={() => Delete(ele.id, ele.name)} />
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                  {companyData.length > 0 && <PageNation data={companyData} setPageRecords={setPageRecords} pageRecords={pageRecords} />}

                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Add/Edit Company */}
        <Modal isOpen={isEdit}
          size='sm-6'
          toggle={() => setIsEdit(!isEdit)}>
          <ModalHeader toggle={() => setIsEdit(!isEdit)}>
            {isAdd === true ? "Create New Company" : "Edit Company"}
          </ModalHeader>
          <ModalBody>
            <div className='container'>
              <div className='row '>
                <div className='col'>
                  <div>Company Name</div>
                  <input type='text'
                    placeholder='Company Name'
                    className=' form-control form-control-sm'
                    value={companyName}
                    onChange={(e) => setCompanyName(e.target.value)} />
                </div>
                <div className='col'>
                  <div>Website</div>
                  <input type='text'
                    placeholder='Website'
                    className=' form-control form-control-sm'
                    value={website}
                    onChange={(e) => setWebsite(e.target.value)} />
                </div>
              </div>
              <div className='row mt-4'>
                <div className='col'>
                  <div>SAP Subaccount Name</div>
                  {isAdd === true ?
                    <input type='text'
                      placeholder='SAP Subaccount Name'
                      className=' form-control form-control-sm'
                      value={sapSubaccountName}
                      onChange={(e) => setSapSubAccount(e.target.value)} />
                    :
                    <input type='text'
                      className=' fixedInput form-control form-control-sm'
                      value={sapSubaccountName} />
                  }
                </div>
                <div className='col'>
                  <div>SAP Subaccount UUID</div>
                  {isAdd === true ?
                    <input type='text'
                      placeholder='SAP Subaccount UUID'
                      className=' form-control form-control-sm'
                      value={uuid}
                      onChange={(e) => setUuid(e.target.value)} />
                    :
                    <input type='text'
                      className=' fixedInput form-control form-control-sm '
                      value={uuid} />
                  }
                </div>
              </div>
              <div className='row mt-4'>
                <div className='col'>
                  <div>Phone</div>
                  <input type='text'
                    placeholder='Phone'
                    className=' form-control form-control-sm'
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)} />
                </div>
                <div className='col'>
                  <div>IsActive</div>
                  <Form.Select
                    className='form-control form-control-sm'
                    value={isActive}
                    onChange={(e) => setIsActive(e.target.value)}>
                    <option value={true}>Active</option>
                    <option value={false}>InActive</option>
                  </Form.Select>
                </div>
              </div>
              <div className='row mt-4'>
                <div className='col'>
                  <div>CORS Domain</div>
                  {isAdd === true ?
                    <input type='text'
                      placeholder='CROS Domain'
                      className=' form-control form-control-sm'
                      value={crosDomainTxt}
                      onChange={(e) => setCrosDomainTxt(e.target.value)} />
                    :
                    <input type='text'
                      className=' fixedInput form-control form-control-sm'
                      value={crosDomainTxt} />
                  }
                </div>
                <div className='col'>
                  <div>Subscriptions</div>
                  <input type='text'
                    placeholder='Subscriptions'
                    className=' form-control form-control-sm'
                    value={subscriptions}
                    onChange={(e) => setSubscriptions(e.target.value)} />
                </div>
              </div>
              <div className='row mt-4'>
                <div>CI Server</div>
                <div className='col-6'>
                  <Form.Select
                    className='form-control form-control-sm '
                    value={jenkinId}
                    onChange={(e) => setJenkinId(e.target.value)}>
                    <option vlaue="">Select CI Server</option>
                    {jenkinsData.map((ele, index) => {
                      return (
                        <option value={ele.id} key={index}>{ele.name}</option>
                      )
                    })}
                  </Form.Select>
                </div>
              </div>
              <div className='row mt-4'>
                <div className='col'>
                  <div>Total AdminLicenses</div>
                  <input type='text'
                    placeholder='Total AdminLicenses'
                    className=' form-control form-control-sm'
                    value={totalAdminLicenses}
                    onChange={(e) => setTotalAdminLicenses(e.target.value)} />
                </div>
                <div className='col'>
                  <div>Total UserLicenses</div>
                  <input type='text'
                    placeholder='Total UserLicenses'
                    className=' form-control form-control-sm'
                    value={totalUserLicenses}
                    onChange={(e) => setTotalUserLicenses(e.target.value)} />
                </div>
              </div>
              <div className='row mt-4'>
                <div className='col'>
                  <div>Total Lite User Licenses</div>
                  <input type='text'
                    placeholder='Total UserLicenses'
                    className=' form-control form-control-sm'
                    value={totalLiteUserLicenses}
                    onChange={(e) => setTotalLiteUserLicenses(e.target.value)} />
                </div>
                <div className='col'>
                  <div>Volume Path</div>
                  {isAdd ? <input type='text'
                    placeholder='Volume Path'
                    className='form-control form-control-sm'
                    value={volume}
                    onChange={(e) => setVolume(e.target.value)} />
                    :
                    <input type='text'
                      className=' fixedInput form-control form-control-sm'
                      value={volume} />
                  }

                </div>
              </div>

              <div className='row mt-4'>
                <div className='col-6' >
                  <div className='row '>
                    <div className='col-6 d-flex'>
                      <input type='checkbox'
                        checked={isSapCloud}
                        onClick={() => setIsSapCloud(!isSapCloud)}
                      />
                      <div className='m-2'>SAPCLOUD License</div>
                    </div>
                    {
                      isSapCloud
                      &&
                      <div className='row ms-2'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={cloudEnveronments}
                            onChange={(e) => setCloudEnvironmensts(e.target.value)} />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={cloudStartDate}
                              min={currentDate}
                              onChange={(e) => setCloudStartDate(e.target.value)} />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={cloudExpiryDate}
                              min={currentDate}
                              onChange={(e) => setCloudExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>

                  <div className=' row'>
                    <div className='col-6 d-flex'>
                      <input type='checkbox'
                        checked={isABAP}
                        onClick={() => setIsABAP(!isABAP)} />
                      <div className='m-2'>ABAP License</div>
                    </div>
                    {isABAP &&
                      <div className='row ms-2'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={abapEnveronments}
                            onChange={(e) => setAbapEnvironmensts(e.target.value)}
                          />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={abapStartDate}
                              min={currentDate}
                              onChange={(e) => setAbapStartDate(e.target.value)}
                            />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={abapExpiryDate}
                              min={currentDate}
                              onChange={(e) => setAbapExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>

                  <div className=' row'>
                    <div className='col-6 d-flex'>
                      <input type='checkbox'
                        checked={isSapCpi}
                        onClick={() => setIsSapCpi(!isSapCpi)} />
                      <div className='m-2'>SAPCPI License</div>
                    </div>
                    {isSapCpi &&
                      <div className='row ms-2'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={sapCpiEnveronments}
                            onChange={(e) => setSapCpiEnvironmensts(e.target.value)}
                          />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={sapCpiStartDate}
                              min={currentDate}
                              onChange={(e) => setSapCpiStartDate(e.target.value)}
                            />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={sapCpiExpiryDate}
                              min={currentDate}
                              onChange={(e) => setSapCpiExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>

                  <div className='row'>
                    <div className='col-6 d-flex'>
                      <input type='checkbox'
                        checked={isDatasphere}
                        onClick={() => setIsDatasphere(!isDatasphere)} />
                      <div className='m-2'>SAP Datasphere License</div>
                    </div>
                    {isDatasphere &&
                      <div className='row ms-2'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={DatasphereEnveronments}
                            onChange={(e) => setDatasphereEnvironmensts(e.target.value)}
                          />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={DatasphereStartDate}
                              min={currentDate}
                              onChange={(e) => setDatasphereStartDate(e.target.value)}
                            />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={DatasphereExpiryDate}
                              min={currentDate}
                              onChange={(e) => setDatasphereExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>
                </div>
                <div className='col-6' >
                  <div className=' row'>
                    <div className=' d-flex'>
                      <input type='checkbox'
                        checked={isHana}
                        onClick={() => setIsHana(!isHana)} />
                      <div className='m-2'>HANAXSA License</div>
                    </div>
                    {isHana &&
                      <div className='row ms-2'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={hanaEnveronments}
                            onChange={(e) => setHanaEnvironmensts(e.target.value)}
                          />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              name="startDate"
                              value={hanaStartDate}
                              min={currentDate}

                              onChange={(e) => setHanaStartDate(e.target.value)}
                            />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={hanaExpiryDate}
                              min={currentDate}
                              onChange={(e) => setHanaExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>

                  <div className=' row'>
                    <div className='col-6 d-flex'>
                      <input type='checkbox'
                        checked={isOnPremise}
                        onClick={() => setIsOnPremise(!isOnPremise)} />
                      <div className='m-2'>ONPREMISE License</div>
                    </div>
                    {isOnPremise &&
                      <div className='row ms-3'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={onPremiseEnveronments}
                            onChange={(e) => setOnPremiseEnvironmensts(e.target.value)}
                          />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={onPremiseStartDate}
                              min={currentDate}
                              onChange={(e) => setOnPremiseStartDate(e.target.value)}
                            />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={onPremiseExpiryDate}
                              min={currentDate}
                              onChange={(e) => setOnPremiseExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>

                  <div className=' row'>
                    <div className='col-6 d-flex'>
                      <input type='checkbox'
                        checked={isSac}
                        onClick={() => setIsSac(!isSac)} />
                      <div className='m-2'>SAC License</div>
                    </div>
                    {isSac &&
                      <div className='row ms-2'>
                        <div className='col-6'>
                          <div>Environments</div>
                          <input type='text'
                            placeholder='Environments'
                            className='form-control form-control-sm'
                            value={SacEnveronments}
                            onChange={(e) => setSacEnvironmensts(e.target.value)}
                          />
                        </div>
                        <div className='row mt-2'>
                          <div className='col'>
                            <div>StartDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={SacStartDate}
                              min={currentDate}
                              onChange={(e) => setSacStartDate(e.target.value)}
                            />
                          </div>
                          <div className='col'>
                            <div>ExpiryDate</div>
                            <input type='date'
                              className='form-control form-control-sm'
                              value={SacExpiryDate}
                              min={currentDate}
                              onChange={(e) => setSacExpiryDate(e.target.value)}
                            />
                          </div>
                        </div>
                      </div>
                    }
                  </div>
                </div>
              </div>

            </div>

          </ModalBody>
          <ModalFooter>
            <buttom className='btn btn-primary'
              onClick={() => Validation()}>Save</buttom>
          </ModalFooter>
        </Modal>

        {/* Delete Company */}
        <Modal isOpen={isDelete}
          size='sm-6'
          toggle={() => setIsDelete(!isDelete)}>
          <ModalHeader toggle={() => setIsDelete(!isDelete)}>
            Delete Company
          </ModalHeader>
          <ModalBody>
            Are you sure do you want to delete <span className='fw-bold'>{companyName} ?</span>
          </ModalBody>
          <ModalFooter>
            <button className='btn btn-danger' onClick={DeleteCompany}>
              Delete
            </button>
          </ModalFooter>
        </Modal>

        {/* TenantSettingConfig */}
        <Modal isOpen={isTenantConfig}
          toggle={() => setIsTenantConfig(!isTenantConfig)}
          style={{ width: "100%" }} >
          <ModalHeader toggle={() => setIsTenantConfig(!isTenantConfig)}>
            <div>
              Tenant Settings Config
            </div>
          </ModalHeader>
          <ModalBody>
            <div className='container'>
              <div className='d-flex w-100'>
                <div className='tenantName w-50 me-2'>
                  <label>Tenant Name</label>
                  <input type='text'
                    className=' fixedInput form-control'
                    value={sapSubaccountName} />

                </div>
                <div className='secretKey w-50 ms-2'>
                  <label>Secret Key</label>
                  <input type='text'
                    className='form-control'
                    value={secretKey}
                    onChange={(e) => setSecretKey(e.target.value)} />
                </div>
              </div>
              <div className='d-flex w-100 mt-2 me-2'>
                <div className='refresh-time w-50 me-2'>
                  <label>Transport Buffer Queue Refresh Rate Time</label>
                  <input type='text'
                    className='form-control'
                    value={refreshTime}
                    onChange={(e) => setRefreshTime(e.target.value)} />
                </div>
                <div className='tdcTimeZone w-50 ms-2'>
                  <label>Trasport Domain Controller TimeZone</label>
                  {/* <Form.Select aria-label="Default select example"
                  value={tdcTimeZone}
                  onChange={(e) => setTdcTimeZone(e.target.value)}>
                  {TimeZones.map((timeZone) =>
                    <option value={timeZone} key={timeZone}>{timeZone}</option>

                  )}
                </Form.Select> */}
                  <input type='text' className='form-control'
                    value={tdcTimeZone} onChange={(e) => setTdcTimeZone(e.target.value)} />
                </div>
              </div>
              <div className='d-flex mt-4 w-100'>
                <div className='d-block w-50 me-2' >
                  <div className='mb-1'>Transport Request Config</div>
                  <div className='trconfig ms-3'>
                    <div className='d-flex  text-center' >
                      <input type='checkbox' checked={isImpactanalysis}
                        onClick={() => setIsImpactanalysis(!isImpactanalysis)} />
                      <label className='ms-1'> Impact Analysis</label>
                    </div>
                    {isImpactanalysis && <div className='ms-4' style={{ transitionDuration: " 0.5s;" }}>
                      <div className='mt-2'>
                        <label>Number Of Iterations</label>
                        <input type='text'
                          className='form-control'
                          name='noOfIterations'
                          value={impactanalysis.noOfIterations}
                          onChange={(e) => handleTrConfig(e)} />
                      </div>
                      <div className='mt-2'>
                        <label>Wait Time</label>
                        <input type='text'
                          className='form-control'
                          name='waitTime'
                          value={impactanalysis.waitTime}
                          onChange={(e) => handleTrConfig(e)} />
                      </div>

                    </div>
                    }
                    <div className='d-flex mt-2' >
                      <input type='checkbox' checked={isImportOneByOne}
                        onClick={() => setIsImportOneByOne(!isImportOneByOne)} />
                      <label className='ms-2'> Import OneByOne</label>
                    </div>
                    <div className='mt-2'>
                      <label>RO SAP API version</label>
                      <Form.Select
                        className='form-control form-control-sm'
                        value={roSapApiVersion}
                        onChange={(e) => setRoSapApiVersion(e.target.value)}>
                        <option value="latest">Latest</option>
                        <option value="v1">V1</option>
                      </Form.Select>
                    </div>
                  </div>
                  <div className=' promotion'>
                    <div className='promotion mt-3'>
                      <div className='d-flex ' >
                        <input type='checkbox' checked={isPromotion}
                          onClick={() => setIsPromotion(!isPromotion)} />
                        <label className='ms-1'> Promotion On TR Release</label>
                      </div>
                      {isPromotion && <div className='ms-4' style={{ transitionDuration: " 0.5s;" }}>
                        <div className='mt-2'>
                          <label>Type</label>
                          <input type='text'
                            className=' fixedInput form-control'
                            name='type'
                            value={promotionData.type} />
                        </div>
                        <div div className='mt-2'>
                          <label>Wait Time </label>
                          <input type='text'
                            className='form-control'
                            name='waittime'
                            value={promotionData.waittime}
                            onChange={(e) => handlePromotion(e)} />
                          <div className="fs-6 justify-content-center d-inline-flex" style={{ color: "#ffaa3c" }} ><TiInfoOutline style={{ fontSize: 20 }} />
                            <span style={{ fontSize: 12 }}>  waiting time to promote after transport release</span>
                          </div>
                        </div>
                        <div className='row mt-2'>
                          <div className='col d-flex' >
                            <input type='checkbox' name='agile'
                              checked={promotionData.agile}
                              onChange={(e) => handlePromotion(e)} />
                            <div className='ms-1'> User Stories Enabled</div>
                          </div>
                        </div>
                      </div>
                      }
                    </div>
                  </div>
                  <div className='row mt-4'>
                    <div className='col d-flex ' >
                      <input type='checkbox' checked={scheduler} onChange={() => setScheduler(!scheduler)} />
                      <div className='ms-1'>Enable Scheduler</div>
                    </div>
                  </div>
                  <div className='row mt-2'>
                    <div className='col d-flex ' >
                      <input type='checkbox' checked={apimgmt} onChange={() => setApimgmt(!apimgmt)} />
                      <div className='ms-1'>Enable CPI API Management </div>
                    </div>
                  </div>
                  <div className='row mt-2'>
                    <div className='col d-flex' >
                      <input type='checkbox' checked={usComponentReq} onChange={() => setUsComponentReq(!usComponentReq)} />
                      <div className='ms-1'>Enable User Story Component Field as Required</div>
                    </div>
                  </div>
                  <div className='row mt-2'>
                    <div className='col d-flex' >
                      <input type='checkbox' checked={ignoreCVersForImport} onChange={() => setIgnoreCVersForImport(!ignoreCVersForImport)} />
                      <div className='ms-1'>Enable Ignore Component Version For TR Imports</div>
                    </div>
                  </div>
                </div>
                <div className='cpi_alias w-50 ms-2 '>
                  <div className='d-flex text-center '>
                    <input type='checkbox' checked={isAlias} onClick={() => setIsAlias(!isAlias)} />
                    <div className=' ms-1 text-sm'> CPI Artifact Alias for Deployment </div>
                  </div>
                  {isAlias && <div className='ms-4'>
                    <div className='environments mt-2'>
                      <div className='d-flex justify-content-end'>
                        <div className='btn btn-primary btn-sm' onClick={() => addEnv()} >Add Environment</div>
                      </div>
                      {
                        envData.length > 0 && envData.map((val, index) =>
                          <div className='d-flex justify-content-between'>
                            <div>
                              <label className='mt-2'>Env Id</label>
                              <input type='number'
                                className='form-control'
                                placeholder='Enter Environment Id'
                                name='envId'
                                value={val.envId}
                                onChange={(e) => handleChange(e, index)} />
                            </div>
                            <div className='ms-2'>
                              <label className='mt-2'>AliasType</label>
                              <Form.Select
                                className='alias'
                                name='aliasType'
                                value={val.aliasType}
                                onChange={(e) => handleChange(e, index)}>
                                <option value={0}>perfix</option>
                                <option value={1}>suffix</option>
                                <option value={2}>replace</option>
                              </Form.Select>
                            </div>
                            {val.aliasType == 2 ?
                              <div className='ms-2'>
                                <label className='mt-2'>Find</label>
                                <input type='text'
                                  className='form-control'
                                  placeholder='Enter Find Str'
                                  name='findStr'
                                  value={val.findStr}
                                  onChange={(e) => handleChange(e, index)} />
                              </div> : ""
                            }
                            <div className='ms-2'>
                              <label className='mt-2'>Replace</label>
                              <input type='text'
                                className='form-control'
                                placeholder='Enter Alias Str'
                                name='aliasStr'
                                value={val.aliasStr}
                                onChange={(e) => handleChange(e, index)} />
                            </div>
                            <div className='ms-2' style={{ marginTop: "2.2rem" }} onClick={() => handleDelete(index)}>
                              <label className='btn btn-outline-danger btn-sm'>< AiOutlineDelete /></label>
                            </div>
                          </div>
                        )}

                    </div>
                  </div>}
                </div>
              </div>
            </div>
          </ModalBody>
          <ModalFooter>
            <button className='btn btn-primary btn-sm ' onClick={() => saveTenantSettingConfig()}>Save</button>
          </ModalFooter>
        </Modal>
        <ToastContainer />
      </div>
    </div>
  )
}

export default CompanyManagement