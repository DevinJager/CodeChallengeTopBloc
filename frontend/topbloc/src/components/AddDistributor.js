import React, { useState } from 'react';
import axios from 'axios';

function AddDistributor() {
  const [name, setName] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:4567/add_distributors', {
        name
      });
      setMessage('Distributor added successfully!');
      setName('');
    } catch (error) {
      setMessage(`Error: ${error.response?.data || error.message}`);
    }
  };

  return (
    <div className="form-container">
      <h3>Add New Distributor</h3>
      {message && <div className="message">{message}</div>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Name:</label>
          <input 
            type="text" 
            value={name} 
            onChange={(e) => setName(e.target.value)} 
            required 
          />
        </div>
        <button type="submit">Add Distributor</button>
      </form>
    </div>
  );
}

export default AddDistributor;