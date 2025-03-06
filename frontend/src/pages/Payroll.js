import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { DocumentTextIcon } from '@heroicons/react/24/outline';

const Payroll = () => {
  const [payrollData, setPayrollData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedMonth, setSelectedMonth] = useState(new Date().toISOString().slice(0, 7));

  useEffect(() => {
    fetchPayrollData();
  }, [selectedMonth]);

  const fetchPayrollData = async () => {
    try {
      const response = await axios.get(`/api/payroll/${selectedMonth}`);
      setPayrollData(response.data);
    } catch (error) {
      toast.error('Failed to fetch payroll data');
    } finally {
      setLoading(false);
    }
  };

  const generatePayroll = async () => {
    try {
      setLoading(true);
      await axios.post(`/api/payroll/generate/${selectedMonth}`);
      toast.success('Payroll generated successfully');
      fetchPayrollData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to generate payroll');
    } finally {
      setLoading(false);
    }
  };

  const downloadPayrollReport = async () => {
    try {
      const response = await axios.get(`/api/payroll/${selectedMonth}/report`, {
        responseType: 'blob',
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `payroll-${selectedMonth}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      toast.error('Failed to download payroll report');
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="px-4 sm:px-6 lg:px-8">
      <div className="sm:flex sm:items-center">
        <div className="sm:flex-auto">
          <h1 className="text-2xl font-semibold text-gray-900">Payroll</h1>
          <p className="mt-2 text-sm text-gray-700">
            Monthly payroll information for all employees.
          </p>
        </div>
        <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none space-x-3">
          <input
            type="month"
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
          />
          <button
            onClick={generatePayroll}
            className="inline-flex items-center justify-center rounded-md border border-transparent bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700"
          >
            Generate Payroll
          </button>
          <button
            onClick={downloadPayrollReport}
            className="inline-flex items-center justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50"
          >
            <DocumentTextIcon className="-ml-1 mr-2 h-5 w-5 text-gray-400" />
            Download Report
          </button>
        </div>
      </div>

      <div className="mt-8 flex flex-col">
        <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div className="inline-block min-w-full py-2 align-middle">
            <div className="overflow-hidden shadow-sm ring-1 ring-black ring-opacity-5">
              <table className="min-w-full divide-y divide-gray-300">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Employee
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Department
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Position
                    </th>
                    <th className="px-6 py-3 text-right text-sm font-semibold text-gray-900">
                      Hours Worked
                    </th>
                    <th className="px-6 py-3 text-right text-sm font-semibold text-gray-900">
                      Hourly Rate
                    </th>
                    <th className="px-6 py-3 text-right text-sm font-semibold text-gray-900">
                      Gross Pay
                    </th>
                    <th className="px-6 py-3 text-right text-sm font-semibold text-gray-900">
                      Deductions
                    </th>
                    <th className="px-6 py-3 text-right text-sm font-semibold text-gray-900">
                      Net Pay
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {payrollData.map((item) => (
                    <tr key={item.employeeId}>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                        {item.employeeName}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                        {item.department}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                        {item.position}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-right text-gray-900">
                        {item.hoursWorked}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-right text-gray-900">
                        ${item.hourlyRate.toFixed(2)}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-right text-gray-900">
                        ${item.grossPay.toFixed(2)}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-right text-gray-900">
                        ${item.deductions.toFixed(2)}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-right text-gray-900">
                        ${item.netPay.toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-gray-50">
                  <tr>
                    <td colSpan="5" className="px-6 py-3 text-right text-sm font-semibold text-gray-900">
                      Total:
                    </td>
                    <td className="whitespace-nowrap px-6 py-3 text-sm text-right text-gray-900 font-semibold">
                      ${payrollData.reduce((sum, item) => sum + item.grossPay, 0).toFixed(2)}
                    </td>
                    <td className="whitespace-nowrap px-6 py-3 text-sm text-right text-gray-900 font-semibold">
                      ${payrollData.reduce((sum, item) => sum + item.deductions, 0).toFixed(2)}
                    </td>
                    <td className="whitespace-nowrap px-6 py-3 text-sm text-right text-gray-900 font-semibold">
                      ${payrollData.reduce((sum, item) => sum + item.netPay, 0).toFixed(2)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Payroll;
