import React from 'react';
import {Link} from 'react-router-dom';
import { context } from '../context';
import Shop from '../components/Shop';
import '../App.css';

class Main extends React.Component {
    static contextType = context;

    constructor() {
        super();
        this.state = {
        }
    }

    render() {
        const { shop_list } = this.context;

        return (
            <div className="App">
                <header className="App-header">
                    <div className="Toolbar">
                        分店列表
                        <Link className="AddButton" to="/add">+</Link>
                    </div>
                    {shop_list.map((shop) => {
                        return (<Shop name = {shop.name}/>);
                    })}
                </header>
            </div>
        )    
    }
}

export default Main;
